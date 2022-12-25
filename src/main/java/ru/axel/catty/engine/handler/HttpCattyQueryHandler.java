package ru.axel.catty.engine.handler;

import org.jetbrains.annotations.NotNull;
import ru.axel.catty.engine.utilites.RegexPatterns;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;

/**
 * Обработчик входящих запросов.
 */
public abstract class HttpCattyQueryHandler implements CompletionHandler<Integer, Map<String, Object>> {
    protected final Logger logger;
    protected final AsynchronousSocketChannel client;
    private final int limitAllocateBufferForRequest;

    public HttpCattyQueryHandler(AsynchronousSocketChannel clientChannel, int limitBuffer, Logger loggerInstance) {
        client = clientChannel;
        limitAllocateBufferForRequest = limitBuffer;
        logger = loggerInstance;

        if (logger.isLoggable(Level.FINEST)) logger.finest("Handler create with client: " + clientChannel);
    }

    /**
     * Метод должен реализовать наполнение буфера данными ответа клиенту.
     * Следует помнить, что блокировка внутри метода - будет блокировать весь поток.
     * @param requestBuffer буфера с данными запроса от клиента.
     * @return буфера с данными ответа клиенту.
     */
    protected abstract ByteBuffer responseBuffer(ByteBuffer requestBuffer);

    /**
     * Invoked when an operation has completed.
     *
     * @param result     The result of the I/O operation.
     * @param attachment The object attached to the I/O operation when it was initiated.
     */
    @Override
    public void completed(Integer result, @NotNull Map<String, Object> attachment) {
//        logger.severe("Result: " + result);
//        logger.severe("Attachment: " + attachment);
//        logger.severe("Client: " + client);

        final ClientActions action = (ClientActions) attachment.get("action");

        /*
            Есть 2 кейса переполнения буфера:
            1. result == buffer.capacity()
            2. result == 65536 (пока не рассматриваем, так как буфер меньше)

            Есть кейс когда буфер не переполнен, но прочитан не до конца.
            Есть кейс, когда буфер сразу прочитан стандартная обработка без переполнения.
         */

        if (action.equals(ClientActions.READ)) {
//            logger.severe("Action: " + action.name());
            final ByteBuffer buffer = (ByteBuffer) attachment.get("buffer");

            // Если буффер заполнен или выделялся новый буфер(т.е. идет повторное чтение).
            if (result == buffer.capacity() || attachment.containsKey("newBuffer")) {
                try { // Пробуем аллоцировать новый буфер или продолжаем читать, если уже была аллокация
                    bufferRead(buffer, attachment);
                    buffer.clear();

                    // Если чтение буфера не закончено, читаем сообщение дальше
                    if (!attachment.containsKey("finished")) {
                        client.read(buffer, attachment, this);
                    }
                } catch (Throwable e) {
                    failed(e, attachment);
                }
            } else {
                attachment.put("action", ClientActions.WRITE);
                completed(result, attachment);
            }
        } else if (action.equals(ClientActions.WRITE)) {
            final var buffer = attachment.containsKey("newBuffer")
                ? (ByteBuffer) attachment.get("newBuffer")
                : (ByteBuffer) attachment.get("buffer");

            attachment.put("action", ClientActions.SEND);

            try {
                client.write(responseBuffer(buffer), attachment, this);
            } catch (Throwable exc) {
                logger.severe("Ошибка записи ответа в канал клиента: " + exc.getLocalizedMessage());
                failed(exc, attachment);
            }
        } else if (action.equals(ClientActions.SEND)) {
            if (logger.isLoggable(Level.FINEST)) logger.finest("Action: " + action.name());
            attachment.put("action", ClientActions.READ);

            try {
//                client.shutdownOutput();
                client.close();

                if (logger.isLoggable(Level.FINEST)) logger.finest("Send is completed");
            } catch (IOException e) {
                failed(e, attachment);
            }
        } else {
            if (logger.isLoggable(Level.FINEST)) logger.finest("Client without attachment action: " + client);
        }
    }

    /**
     * Invoked when an operation fails.
     *
     * @param exc        The exception to indicate why the I/O operation failed
     * @param attachment The object attached to the I/O operation when it was initiated.
     */
    @Override
    public void failed(@NotNull Throwable exc, @NotNull Map<String, Object> attachment) {
        logger.severe("Attachment: " + attachment);
        logger.throwing(this.getClass().getName(), "completed", exc);
        exc.printStackTrace();

        try {
            client.close();
            if (logger.isLoggable(Level.FINEST)) logger.finest("Client close with exception");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Метод обрабатывает буфер в случае его переполнения, выделяя новый.
     * @param oldBuffer буфер.
     * @param attachment состояние запроса.
     * @throws ExceptionExcessLimitAllocateBufferForRequest превышение лимита буфера.
     */
    private void bufferRead(
        @NotNull ByteBuffer oldBuffer,
        @NotNull Map<String, Object> attachment
    ) throws ExceptionExcessLimitAllocateBufferForRequest {
        final String receiveMsg = new String(oldBuffer.array()).trim(); // сообщение от клиента в строке

        if (!attachment.containsKey("newBuffer")) { // буфер еще не выделялся
            final Matcher matcherContentLength = RegexPatterns.contentLength(receiveMsg); // ищем заголовок Content-Length
            final Matcher matcherBoundary = RegexPatterns.boundary(receiveMsg); // ищем разделитель партий тела

            if (matcherBoundary.find()) {
                attachment.put("boundary", matcherBoundary.group(1));
            }

            if (matcherContentLength.find()) { // если заголовок найден, аллоцируем новый буфер
                final int contentLength = Integer.parseInt(matcherContentLength.group(1).trim());
                final int newBufferSize = contentLength + oldBuffer.capacity();

                if (newBufferSize > limitAllocateBufferForRequest) {
                    throw new ExceptionExcessLimitAllocateBufferForRequest(
                        "Размер запроса превышен, лимит: " + limitAllocateBufferForRequest + ", " +
                        "запрошено: " + newBufferSize
                    );
                }

                // аллоцируем новый буфер
                final ByteBuffer newBuffer = ByteBuffer.allocate(newBufferSize);
                if (logger.isLoggable(Level.FINEST)) logger.finest("Аллоцирован новый буфер: " + newBufferSize);

                // записываем уже прочитанную информацию
                newBuffer.put(oldBuffer.array(), 0, oldBuffer.position());

//                attachment.put("action", "read");
                attachment.put("newBuffer", newBuffer);
            } else {
                throw new ExceptionExcessLimitAllocateBufferForRequest("Не найден заголовок Content-Length");
            }
        } else {
            final String boundary = (String) attachment.get("boundary");
            final Matcher matcher = RegexPatterns.boundaryFinished(boundary, receiveMsg);

            final ByteBuffer newBuffer = (ByteBuffer) attachment.get("newBuffer");
            newBuffer.put(oldBuffer.array(), 0, oldBuffer.position());

            if (matcher.find()) {
                if (logger.isLoggable(Level.FINEST)) logger.finest("Конец запроса");

                oldBuffer.clear();
                attachment.put("finished", true);
                attachment.put("action", ClientActions.WRITE);

                completed(oldBuffer.position(), attachment);
            }
        }
    }
}
