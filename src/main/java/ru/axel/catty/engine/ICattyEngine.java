package ru.axel.catty.engine;

import java.io.IOException;
import java.util.logging.Logger;

public interface ICattyEngine extends AutoCloseable {
    void setLogger(Logger loggerInstance);
    void setTimeToReadBuffer(Long timeSeconds);
    void startServer() throws IOException;
    void stopServer();
}
