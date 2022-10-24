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
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    static {
        MiniLogger.setLogLevel(Level.ALL);
    }

    private static final Logger logger = MiniLogger.getLogger(Main.class);
    private static final IRouting routing = new Routing(logger);
    private static final Plugins plugins = new Plugins(logger);

    public static void main(String[] args) {
        plugins.addPipelines("default headers", (request, response) -> {
            response.addHeader(Headers.DATE, String.valueOf(new Date()));
            response.addHeader(Headers.SERVER, "Catty");
            response.addHeader(Headers.CONNECTION, "close");
        });

        final ICattyRoute routeTest = new Route("/test", "GET", (request, response) -> {
            logger.severe("Request path: " + request.getPath());
            logger.severe("Params: " + request.getQueryParam("test"));

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
            logger.severe("Request path: " + request.getPath());
            logger.severe("Params id: " + request.getParams("id"));

            response.addHeader(Headers.CONTENT_TYPE, "application/json; charset=UTF-8");
            response.respond(ResponseCode.OK, "{\"status\": \"OK\"}");
        });

        final ICattyRoute routeTestCookie = new Route("/cookie/set", "GET", (request, response) -> {
            final ISetCookie cookie = new SetCookie("test", "test")
                .setExpires(new Date())
//                .setDomain("localhost")
                .setHttpOnly(true)
                .setMaxAge(3600)
//                .setPath("/")
                .setSecure(true)
                .setSameSite(SameSite.STRICT);

            logger.severe(cookie.toString());
            response.setCookie(cookie);

            response.respond(ResponseCode.OK, "OK");
        });

        routing.addRoute(routeTest);
        routing.addRoute(routeParams);
        routing.addRoute(routeTestCookie);
        routing.staticResourceFiles("/static");

        try(final ICattyEngine engine = new CattyEngine(
            new InetSocketAddress(8080),
            10,
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
                    if (routing.takeRoute(request).isPresent()) {
                        plugins.exec(request, response);

                        request.handle(response);
                    } else {
                        response.setResponseCode(ResponseCode.NOT_FOUND);
                    }
                } catch (Throwable exc) {
                    response.setResponseCode(ResponseCode.INTERNAL_SERVER_ERROR);
                    exc.printStackTrace();
                }

                return response.getByteBuffer();
            } catch (RequestBuildException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}