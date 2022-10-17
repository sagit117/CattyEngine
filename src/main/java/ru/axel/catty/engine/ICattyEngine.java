package ru.axel.catty.engine;

import java.io.IOException;
import java.util.logging.Logger;

public interface ICattyEngine {
    void setLogger(Logger loggerInstance);
    void startServer() throws IOException;
    void stopServer();
}
