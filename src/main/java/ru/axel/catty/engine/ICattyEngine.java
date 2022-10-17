package ru.axel.catty.engine;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.logging.Logger;

public interface ICattyEngine {
    void setLogger(Logger loggerInstance);
    void startServer() throws IOException;
    void stopServer();
}
