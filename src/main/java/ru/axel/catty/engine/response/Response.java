package ru.axel.catty.engine.response;

import org.jetbrains.annotations.NotNull;
import ru.axel.catty.engine.headers.Headers;
import ru.axel.catty.engine.headers.IHeaders;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Класс содержит данные ответа клиенту.
 */
public class Response implements IHttpCattyResponse {
    private final Logger logger;
    private final HashMap<String, String> headers = new HashMap<>();
    private byte[] body;
    private int responseCode;

    public Response(Logger loggerInstance) {
        logger = loggerInstance;
    }

    /**
     * Метод формирует строку ответа
     * @return строка ответа
     */
    private @NotNull String getHeadResponse() {
        final StringBuilder responseLines = new StringBuilder();

        responseLines.append("HTTP/1.1 ").append(responseCode).append("\r\n");
        headers.forEach((key, value) -> {
            responseLines.append(key).append(": ").append(value).append("\r\n");
        });
        responseLines.append("\r\n");

        return responseLines.toString();
    }

    /**
     * Метод добавляет заголовок в ответ
     * @param header имя заголовка
     * @param value значение заголовка
     */
    @Override
    public void addHeader(@NotNull IHeaders header, String value) {
        headers.put(header.getHeaderName(), value);
    }
    @Override
    public void addHeader(@NotNull String header, String value) {
        headers.put(header, value);
    }

    /**
     * Метод добавляет тело ответа
     * @param bodyString тело ответа
     */
    @Override
    public void setBody(@NotNull String bodyString) {
        final byte[] bytes = bodyString.getBytes(StandardCharsets.UTF_8);

        addHeader(Headers.CONTENT_LENGTH, String.valueOf(bytes.length));
        body = bytes;
    }

    /**
     * Метод добавляет тело ответа
     * @param bodyBytes тело ответа
     */
    @Override
    public void setBody(byte @NotNull [] bodyBytes) {
        addHeader(Headers.CONTENT_LENGTH, String.valueOf(bodyBytes.length));
        body = bodyBytes;
    }

    /**
     * Метод устанавливает код ответа
     * @param code код ответа
     */
    @Override
    public void setResponseCode(@NotNull ResponseCode code) {
        responseCode = code.getCode();
        logger.finest("Установлен код ответа: " + code);
    }
    @Override
    public void setResponseCode(int code) {
        responseCode = code;
        logger.finest("Установлен код ответа: " + code);
    }

    /**
     * Метод получает код ответа.
     * @return код ответа.
     */
    @Override
    public int getResponseCode() {
        return responseCode;
    }

    /**
     * Метод возвращает массив байтов ответа
     * @param charset кодировка.
     * @throws IOException ошибка записи байт в поток.
     * @return массив байтов ответа
     */
    @Override
    public byte @NotNull [] getBytes(Charset charset) throws IOException {
        final byte[] arrByte = getHeadResponse().getBytes(charset);

        // ByteArrayOutputStream выбран как более элегантный способ с заделом на будущее, вместо например arraycopy.
        try(final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            outputStream.write(arrByte);

            if (body != null && body.length > 0) outputStream.write(body);

            return outputStream.toByteArray();
        } catch (Throwable exc) {
            exc.printStackTrace();
            throw exc;
        }
    }

    /**
     * Метод возвращает массив байтов ответа в кодировке UTF_8.
     * @throws IOException ошибка записи байт в поток.
     * @return массив байтов ответа
     */
    @Override
    public byte @NotNull [] getBytes() throws IOException {
        return getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Метод возвращает буффер байтов ответа
     * @throws IOException ошибка записи байт в поток.
     * @return буффер байтов ответа
     */
    @Override
    public ByteBuffer getByteBuffer() throws IOException {
        return ByteBuffer.wrap(getBytes());
    }

    /**
     * Метод объединяет установку кода ответа и тела.
     * @param code код ответа.
     * @param body тело ответа.
     */
    @Override
    public void respond(ResponseCode code, String body) {
        setResponseCode(code);
        setBody(body);
    }
    @Override
    public void respond(ResponseCode code, byte[] body) {
        setResponseCode(code);
        setBody(body);
    }

    /**
     * Метод устанавливает в заголовок куки.
     * @param cookie объект хранения куки.
     */
    @Override
    public void setCookie(@NotNull ISetCookie cookie) {
        addHeader(Headers.SET_COOKIE, cookie.toString());
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
