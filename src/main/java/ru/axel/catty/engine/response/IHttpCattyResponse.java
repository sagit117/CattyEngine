package ru.axel.catty.engine.response;

import org.jetbrains.annotations.NotNull;
import ru.axel.catty.engine.headers.Headers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

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
     * @param bodyString тело ответа
     */
    void addBody(@NotNull String bodyString);

    /**
     * Метод добавляет тело ответа
     * @param bodyBytes тело ответа
     */
    void addBody(byte @NotNull [] bodyBytes);

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
     * @param charset кодировка.
     * @throws IOException ошибка записи байт в поток.
     * @return массив байтов ответа
     */
    byte @NotNull [] getBytes(Charset charset) throws IOException;

    /**
     * Метод возвращает массив байтов ответа в кодировке UTF_8.
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
