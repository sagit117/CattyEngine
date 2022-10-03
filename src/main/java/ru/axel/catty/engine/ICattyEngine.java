package ru.axel.catty.engine;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.channels.AsynchronousServerSocketChannel;

public interface ICattyEngine {
    void startServer() throws IOException;
    void loop(@NotNull AsynchronousServerSocketChannel server);
    void stopServer();
}
