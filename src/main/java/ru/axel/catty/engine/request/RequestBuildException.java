package ru.axel.catty.engine.request;

/**
 * Ошибка при создании объекта Request.
 * @see Request
 */
public final class RequestBuildException extends Exception {
    public RequestBuildException(String msg) {
        super(msg);
    }
}
