package ru.axel.catty.engine;

import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Map;

/**
 * Класс для наследования обработчиком запроса
 */
public abstract class EngineHandlerImpl implements CompletionHandler<Integer, Map<String, Object>> {
    protected final AsynchronousSocketChannel client;
    protected final int limitAllocateBufferForRequest;
    public EngineHandlerImpl(AsynchronousSocketChannel client, int limitAllocateBufferForRequest) {
        this.client = client;
        this.limitAllocateBufferForRequest = limitAllocateBufferForRequest;
    }
}
