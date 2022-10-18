package ru.axel.catty;

import ru.axel.catty.engine.CattyEngine;
import ru.axel.catty.engine.handler.HttpCattyQueryHandler;
import ru.axel.catty.engine.headers.Headers;
import ru.axel.catty.engine.request.Request;
import ru.axel.catty.engine.request.RequestBuildException;
import ru.axel.catty.engine.response.Response;
import ru.axel.catty.engine.response.ResponseCode;
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
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = MiniLogger.getLogger(Main.class);
    private static final IRouting routing = new Routing();

    public static void main(String[] args) throws IOException {
        ICattyRoute routeTest = new Route("/test", "GET", (request, response) -> {
            logger.severe("Request path: " + request.getPath());
            logger.severe("Params: " + request.getQueryParam("test"));

            var body = "<!DOCTYPE html>" +
                    "<html>" +
                    "<head>" +
                    "<title>Status</title>" +
                    "<link rel=\"stylesheet\" type=\"text/css\" href=\"/static/index.css\">" +
                    "</head>" +
                    "<body>" +
                    "<h1>Тестовая страница</h1>" +
                    "<form method=\"post\" enctype=\"multipart/form-data\">" +
                    "<input type=\"file\" name=\"file\" multiple>" +
                    "<button type=\"submit\">SUBMIT</button>" +
                    "</form>" +
                    "</body>";

            response.respond(ResponseCode.OK, body);
        });

        routing.addRoute(routeTest);
        routing.staticResourceFiles("/static");

        var engine = new CattyEngine(
            new InetSocketAddress(8080),
            10,
            5000000,
            Handler::new
        );

        engine.startServer();
    }

    static class Handler extends HttpCattyQueryHandler {

        public Handler(AsynchronousSocketChannel clientChannel, int limitBuffer, Logger loggerInstance) {
            super(clientChannel, limitBuffer, loggerInstance);
        }

        @Override
        protected ByteBuffer responseBuffer(ByteBuffer requestBuffer) {
            try {
                var request = new Request(requestBuffer);
                var response = new Response();
                response.addHeader(Headers.DATE, String.valueOf(new Date()));
                response.addHeader(Headers.CONTENT_TYPE, "text/html; charset=UTF-8");
                response.addHeader(Headers.SERVER, "Catty");
                response.addHeader(Headers.CONNECTION, "close");

                try {
                    routing.takeRoute(request).handle(request, response);
                } catch (NullPointerException npe) {
                    response.setResponseCode(ResponseCode.NOT_FOUND);
                }

                return response.getByteBuffer();
            } catch (RequestBuildException | IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
    }
}