package ru.axel.catty.engine.response;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Код ответа сервера.
 */
public enum ResponseCode {
    OK(200),
    BAD_REQUEST(400),
    NOT_FOUND(404),
    INTERNAL_SERVER_ERROR(500),
    ;

    final int code;

    ResponseCode(int code) {
        this.code = code;
    }

    final public int getCode() {
        return code;
    }

    @Contract(pure = true)
    @Override
    final public @NotNull String toString() {
        return code + " " + name();
    }
}
