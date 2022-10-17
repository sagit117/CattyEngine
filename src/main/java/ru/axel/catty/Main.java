package ru.axel.catty;

import ru.axel.catty.engine.CattyEngine;
import ru.axel.catty.engine.handler.HttpCattyQueryHandler;
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
            return ByteBuffer.wrap(
                "HTTP/1.1 200\r\nContent-Type: text/html; charset=UTF-8\r\n\r\nOK".getBytes(Charset.defaultCharset())
            );
        }
    }
}