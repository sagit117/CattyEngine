package ru.axel.catty.engine;

import java.nio.channels.CompletionHandler;
import java.util.Map;

public interface QueryHandler {
    CompletionHandler<Integer, Map<String, Object>> getHandler();
}
