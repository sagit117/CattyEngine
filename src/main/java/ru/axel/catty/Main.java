package ru.axel.catty;

import ru.axel.catty.engine.CattyEngine;
import ru.axel.catty.engine.handler.HttpCattyQueryHandler;
import ru.axel.catty.engine.request.Request;
import ru.axel.catty.engine.request.RequestBuildException;
import ru.axel.catty.engine.response.Response;
import ru.axel.catty.engine.response.ResponseCode;
import ru.axel.logger.MiniLogger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.Charset;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = MiniLogger.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
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
                logger.severe("Request path: " + request.path);
                logger.severe("Params: " + request.getQueryParam("test"));

                var response = new Response();
                return response.respond(ResponseCode.OK, "OK");
            } catch (RequestBuildException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}