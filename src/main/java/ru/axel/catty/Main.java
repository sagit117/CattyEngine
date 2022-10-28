package ru.axel.catty;

import ru.axel.catty.engine.CattyEngine;
import ru.axel.catty.engine.ICattyEngine;
import ru.axel.catty.engine.handler.HttpCattyQueryHandler;
import ru.axel.catty.engine.headers.Headers;
import ru.axel.catty.engine.plugins.Plugins;
import ru.axel.catty.engine.request.IHttpCattyRequest;
import ru.axel.catty.engine.request.Request;
import ru.axel.catty.engine.request.RequestBuildException;
import ru.axel.catty.engine.response.*;
import ru.axel.catty.engine.routing.ICattyRoute;
import ru.axel.catty.engine.routing.IRouting;
import ru.axel.catty.engine.routing.Route;
import ru.axel.catty.engine.routing.Routing;
import ru.axel.logger.MiniLogger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    static {
        MiniLogger.setLogLevel(Level.CONFIG);
    }

    private static final Logger logger = MiniLogger.getLogger(Main.class);
    private static final IRouting routing = new Routing(logger);
    private static final Plugins plugins = new Plugins(logger);
    private static final long answerTimeout = 30;

    public static void main(String[] args) {
        plugins.addPipelines("default headers", (request, response) -> {
            response.addHeader(Headers.DATE, String.valueOf(new Date()));
            response.addHeader(Headers.SERVER, "Catty");
            response.addHeader(Headers.CONNECTION, "close");
        });

        plugins.addPipelines("request id", (request, response) -> {
            var id = UUID.randomUUID();
            request.setParams("REQUEST_ID", id.toString());
            logger.severe("Set request ID: " + id);
        });

        final ICattyRoute routeTest = new Route("/test", "GET", (request, response) -> {
            logger.finest("Request path: " + request.getPath());
            logger.finest("Params: " + request.getQueryParam("test"));

            logger.finest("ID request: " + request.getParams("REQUEST_ID"));

            final String body = """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Status</title>
                    <link rel="stylesheet" type="text/css" href="/static/index.css">
                </head>
                <body>
                    <h1>Тестовая страница</h1>
                    <form method="post" enctype="multipart/form-data">
                        <input type="file" name="file" multiple>
                        <button type="submit">SUBMIT</button>
                    </form>
                </body>
            """;

            response.addHeader(Headers.CONTENT_TYPE, "text/html; charset=UTF-8");
            response.respond(ResponseCode.OK, body);
        });

        final ICattyRoute routeParams = new Route("/params/{id}/get", "GET", (request, response) -> {
            logger.finest("Request path: " + request.getPath());
            logger.finest("Params id: " + request.getParams("id"));

            if (Objects.equals(request.getParams("id"), "1")) {
                System.out.println("SLEEP");
                for (; true; ) {

                }
            }


            response.addHeader(Headers.CONTENT_TYPE, "application/json; charset=UTF-8");
            response.respond(ResponseCode.OK, "{\"status\": \"OK\"}");

            logger.finest("Response for ID: " + request.getParams("REQUEST_ID"));
        });

        final ICattyRoute routeTestCookie = new Route("/cookie/set", "GET", (request, response) -> {
            final ISetCookie cookie = new SetCookie("test", "test")
                .setExpires(new Date())
//                .setDomain("127.0.0.1")
                .setHttpOnly(true)
                .setMaxAge(3600)
//                .setPath("/")
                .setSecure(true)
                .setSameSite(SameSite.STRICT);

            logger.finest(cookie.toString());
            response.setCookie(cookie);

            response.respond(ResponseCode.OK, "OK");
        });

        routing.addRoute(routeTest);
        routing.addRoute(routeParams);
        routing.addRoute(routeTestCookie);
        routing.staticResourceFiles("/static");

        try(final ICattyEngine engine = new CattyEngine(
            new InetSocketAddress(8080),
            1,
            5000000,
            Handler::new
        )) {
            engine.startServer();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    static class Handler extends HttpCattyQueryHandler {

        public Handler(AsynchronousSocketChannel clientChannel, int limitBuffer, Logger loggerInstance) {
            super(clientChannel, limitBuffer, loggerInstance);
        }

        @Override
        protected ByteBuffer responseBuffer(ByteBuffer requestBuffer) {
            try {
                final IHttpCattyRequest request = new Request(requestBuffer, logger);
                final IHttpCattyResponse response = new Response(logger);

                try {
                    var route = routing.takeRoute(request);

                    if (route.isPresent()) {
                        request.setRoute(route.get());
                        plugins.exec(request, response);

                        CompletableFuture
                            .runAsync(() -> {
                                try {
                                    request.handle(response);
                                } catch (IOException | URISyntaxException e) {
                                    response.setResponseCode(ResponseCode.INTERNAL_SERVER_ERROR);
                                    e.printStackTrace();
                                }
                            }, Executors.newWorkStealingPool(1))
                            .orTimeout(answerTimeout, TimeUnit.SECONDS)
                            .get();
                    } else {
                        response.setResponseCode(ResponseCode.NOT_FOUND);
                    }
                } catch (ExecutionException executionException) { // ожидание ответа превышено
                    response.setResponseCode(ResponseCode.INTERNAL_SERVER_ERROR);
                } catch (Throwable exc) {
                    response.setResponseCode(ResponseCode.INTERNAL_SERVER_ERROR);
                    exc.printStackTrace();
                }

                logger.severe("Response code: " + response.getResponseCode());
                logger.severe("Request ID: " + request.getParams("REQUEST_ID"));

                return response.getByteBuffer();
            } catch (RequestBuildException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}