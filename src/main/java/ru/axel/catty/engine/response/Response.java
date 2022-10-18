package ru.axel.catty.engine.response;

import org.jetbrains.annotations.NotNull;
import ru.axel.catty.engine.headers.Headers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * Класс содержит данные ответа клиенту.
 */
public class Response implements IHttpCattyResponse {
    private final HashMap<String, String> headers = new HashMap<>();
    private byte[] body;
    private int responseCode;

    /**
     * Метод формирует строку ответа
     * @return строка ответа
     */
    private @NotNull String getHeadResponse() {
        StringBuilder responseLines = new StringBuilder();

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
    public void addHeader(@NotNull Headers header, String value) {
        headers.put(header.getHeaderName(), value);
    }
    @Override
    public void addHeader(@NotNull String header, String value) {
        headers.put(header, value);
    }

    /**
     * Метод добавляет тело ответа
     * @param body тело ответа
     */
    @Override
    public void addBody(@NotNull String body) {
        var bytes = body.getBytes(StandardCharsets.UTF_8);

        addHeader(Headers.CONTENT_LENGTH, String.valueOf(bytes.length));
        this.body = bytes;
    }
    @Override
    public void addBody(byte @NotNull [] body) {
        addHeader(Headers.CONTENT_LENGTH, String.valueOf(body.length));
        this.body = body;
    }

    /**
     * Метод устанавливает код ответа
     * @param code код ответа
     */
    @Override
    public void setResponseCode(@NotNull ResponseCode code) {
        responseCode = code.getCode();
    }
    @Override
    public void setResponseCode(int code) {
        responseCode = code;
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
     * @throws IOException ошибка записи байт в поток.
     * @return массив байтов ответа
     */
    @Override
    public byte @NotNull [] getBytes() throws IOException {
        var arrByte = getHeadResponse().getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(arrByte);
        if (body != null && body.length > 0) outputStream.write(body);

        return outputStream.toByteArray();
    }

    /**
     * Метод возвращает буффер байтов ответа
     * @throws IOException ошибка записи байт в поток.
     * @return буффер байтов ответа
     */
    public ByteBuffer getByteBuffer() throws IOException {
        return ByteBuffer.wrap(getBytes());
    }

    /**
     * Метод объединяет установку кода ответа и тела.
     * @param code код ответа.
     * @param body тело ответа.
     * @return буффер байтов ответа.
     * @throws IOException ошибка записи байт в поток.
     */
    public void respond(ResponseCode code, String body) throws IOException {
        setResponseCode(code);
        addBody(body);
    }
}
