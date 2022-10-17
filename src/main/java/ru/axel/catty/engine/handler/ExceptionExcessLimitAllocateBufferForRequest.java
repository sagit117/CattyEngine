package ru.axel.catty.engine.handler;

/**
 * Ошибка превышения лимита выделенного для буфера запроса
 */
public final class ExceptionExcessLimitAllocateBufferForRequest extends Exception {
    ExceptionExcessLimitAllocateBufferForRequest(String msg) {
        super(msg);
    }
}
