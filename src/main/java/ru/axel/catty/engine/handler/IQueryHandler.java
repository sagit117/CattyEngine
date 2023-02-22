package ru.axel.catty.engine.handler;

import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Обработчик запроса
 */
public interface IQueryHandler {
    CompletionHandler<Integer, Map<String, Object>> getHandler(
        AsynchronousSocketChannel client,
        long limitAllocateBufferForRequest,
        Logger loggerInstance
    );
}
