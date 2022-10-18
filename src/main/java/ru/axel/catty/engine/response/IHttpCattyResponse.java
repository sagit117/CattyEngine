package ru.axel.catty.engine.response;

import org.jetbrains.annotations.NotNull;
import ru.axel.catty.engine.headers.Headers;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface IHttpCattyResponse {
    /**
     * Метод добавляет заголовок в ответ
     * @param header имя заголовка
     * @param value значение заголовка
     */
    void addHeader(@NotNull Headers header, String value);
    void addHeader(@NotNull String header, String value);

    /**
     * Метод добавляет тело ответа
     * @param body тело ответа
     */
    void addBody(@NotNull String body);
    void addBody(byte @NotNull [] body);

    /**
     * Метод устанавливает код ответа
     * @param code код ответа
     */
    void setResponseCode(@NotNull ResponseCode code);
    void setResponseCode(int code);

    /**
     * Метод получает код ответа.
     * @return код ответа.
     */
    int getResponseCode();

    /**
     * Метод возвращает массив байтов ответа
     * @throws IOException ошибка записи байт в поток.
     * @return массив байтов ответа
     */
    byte @NotNull [] getBytes() throws IOException;

    /**
     * Метод возвращает буффер байтов ответа
     * @throws IOException ошибка записи байт в поток.
     * @return буффер байтов ответа
     */
    ByteBuffer getByteBuffer() throws IOException;

    /**
     * Метод объединяет установку кода ответа и тела.
     * @param code код ответа.
     * @param body тело ответа.
     * @return буффер байтов ответа.
     * @throws IOException ошибка записи байт в поток.
     */
    void respond(ResponseCode code, String body) throws IOException;
}
