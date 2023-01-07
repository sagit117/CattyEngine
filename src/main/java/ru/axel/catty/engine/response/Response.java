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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Класс содержит данные ответа клиенту.
 */
public class Response implements IHttpCattyResponse {
    private final Logger logger;
    private final HashMap<String, String> headers = new HashMap<>();
    private byte[] body;
    private int responseCode;
    private final String httpVersion = "HTTP/1.1";
    private TransformResponse transformResponse;

    public Response(Logger loggerInstance) {
        logger = loggerInstance;
    }

    /**
     * Метод формирует строку ответа
     * @return строка ответа
     */
    private @NotNull String getHeadResponse() {
        final StringBuilder responseLines = new StringBuilder();

        responseLines.append(httpVersion).append(" ").append(responseCode).append("\r\n");
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
        final byte[] bytes = transformResponse == null
            ? bodyString.getBytes(StandardCharsets.UTF_8)
            : transformResponse.transform(bodyString.getBytes(StandardCharsets.UTF_8));

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
        body = transformResponse == null
            ? bodyBytes
            : transformResponse.transform(bodyBytes);
    }

    /**
     * Метод устанавливает код ответа
     * @param code код ответа
     */
    @Override
    public void setResponseCode(@NotNull ResponseCode code) {
        responseCode = code.getCode();
        if (logger.isLoggable(Level.FINEST)) logger.finest("Установлен код ответа: " + code);
    }
    @Override
    public void setResponseCode(int code) {
        responseCode = code;
        if (logger.isLoggable(Level.FINEST)) logger.finest("Установлен код ответа: " + code);
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

//            System.out.println(new String(outputStream.toByteArray()));

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
     * Метод объединяет установку кода ответа и тела, а также добавляет заголовок Headers.CONTENT_TYPE, "text/html; charset=utf-8".
     * @param code код ответа.
     * @param body тело ответа.
     */
    @Override
    public void respondHTML(ResponseCode code, String body) {
        addHeader(Headers.CONTENT_TYPE, "text/html; charset=utf-8");
        setResponseCode(code);
        setBody(body);
    }
    @Override
    public void respondHTML(ResponseCode code, byte[] body) {
        addHeader(Headers.CONTENT_TYPE, "text/html; charset=utf-8");
        setResponseCode(code);
        setBody(body);
    }

    @Override
    public void redirect(String path, boolean isPermanently) {
        setResponseCode(isPermanently ? ResponseCode.MOVED_PERMANENTLY : ResponseCode.FOUND);
        addHeader(Headers.LOCATION.getHeaderName(), path);
    }
    @Override
    public void redirect(String path, boolean isPermanently, byte[] body) {
        setResponseCode(isPermanently ? ResponseCode.MOVED_PERMANENTLY : ResponseCode.FOUND);
        addHeader(Headers.LOCATION.getHeaderName(), path);
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
    public void setTransformMethod(TransformResponse method) {
        transformResponse = method;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
