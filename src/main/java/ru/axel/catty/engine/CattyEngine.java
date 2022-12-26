package ru.axel.catty.engine;

import org.jetbrains.annotations.NotNull;
import ru.axel.catty.engine.handler.ClientActions;
import ru.axel.catty.engine.handler.IQueryHandler;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Движок сервера, создает подключение и слушает указанный порт.
 * Движок ничего не знает об обработки ответа или запроса, за это отвечает класс в параметрах конструктора handler.
 */
public final class CattyEngine implements ICattyEngine {
    private static Logger logger = Logger.getLogger(CattyEngine.class.getName());
    private final InetSocketAddress hostAddress;
    private final ExecutorService pool;
    private final int buffer_size = 16_384; // 16kb
    private volatile boolean stop = false;
    private final IQueryHandler queryHandler;
    private final int limitAllocateBufferForRequest; // максимальный размер буфера для принятия запроса
    private long timeToReadBuffer = 5L; // время ожидания чтения из буфера запроса
    private AsynchronousChannelGroup group;

    /**
     * Конструктор класса
     * @param hostAddress адрес сервера
     * @param poolLimit лимит потоков
     * @param limitAllocateBufferForRequest максимальный размер буфера для принятия запроса
     * @param handler класс обработчик запроса и ответа
     */
    public CattyEngine(
        InetSocketAddress hostAddress,
        int poolLimit,
        int limitAllocateBufferForRequest,
        @NotNull IQueryHandler handler
    ) {
        this.hostAddress = hostAddress;
        pool = Executors.newWorkStealingPool(poolLimit);
        this.limitAllocateBufferForRequest = limitAllocateBufferForRequest;
        queryHandler = handler;
    }
    /**
     * Конструктор класса
     * @param hostAddress адрес сервера
     * @param executor трэд пул
     * @param limitAllocateBufferForRequest максимальный размер буфера для принятия запроса
     * @param handler класс обработчик запроса и ответа
     */
    public CattyEngine(
        InetSocketAddress hostAddress,
        ExecutorService executor,
        int limitAllocateBufferForRequest,
        @NotNull IQueryHandler handler
    ) {
        this.hostAddress = hostAddress;
        pool = executor;
        this.limitAllocateBufferForRequest = limitAllocateBufferForRequest;
        queryHandler = handler;
    }

    /**
     * Метод устанавливает логгер.
     * @param loggerInstance объект логгера.
     */
    @Override
    public void setLogger(Logger loggerInstance) {
        logger = loggerInstance;
    }

    /**
     * Устанавливает время ожидания чтения из буфера запроса
     * @param timeSeconds - время в секундах
     */
    @Override
    public void setTimeToReadBuffer(Long timeSeconds) {
        timeToReadBuffer = timeSeconds;
    }

    /**
     * Метод создает подключение
     * @throws IOException ошибка подключения
     */
    @Override
    public void startServer() throws IOException {
        group = AsynchronousChannelGroup.withThreadPool(pool);
        final AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open(group);
        server.bind(hostAddress);

        logger.info("Сервер запущен на порту: " + hostAddress.getPort());
        loop(server);
    }

    /**
     * Метод останавливает сервер
     */
    @Override
    public void stopServer() {
        stop = true;
        pool.shutdown();
        group.shutdown();
    }

    /**
     * Метод запускает петлю обработки событий
     */
    private void loop(final @NotNull AsynchronousServerSocketChannel server) {
        server.accept(null, new CompletionHandler<>() {
            @Override
            public void completed(final AsynchronousSocketChannel clientChannel, final Object attachment) {
//                try {
//                    clientChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }

                if (server.isOpen()) { // если удалить условия не будет параллелизма в запросах
                    if (logger.isLoggable(Level.FINEST)) logger.finest("Server is accepted");
                    server.accept(null, this);
                }

                if (clientChannel.isOpen()) {
                    if (logger.isLoggable(Level.FINEST)) logger.finest("Server accept client: " + clientChannel);

                    final ByteBuffer buffer = ByteBuffer.allocate(buffer_size);

                    final Map<String, Object> readInfo = new HashMap<>();
                    readInfo.put("action", ClientActions.READ);
                    readInfo.put("buffer", buffer);

                    clientChannel.read(
                        buffer,
                        timeToReadBuffer,
                        TimeUnit.SECONDS,
                        readInfo,
                        queryHandler.getHandler(
                            clientChannel,
                            limitAllocateBufferForRequest,
                            logger
                        )
                    );
                }
            }

            @Override
            public void failed(final Throwable ex, final Object attachment) {
                logger.severe("Ошибка принятия соединения от клиента. Инфо: " + attachment);
                logger.throwing(CattyEngine.class.getName(), "loop", ex);
                ex.printStackTrace();
            }
        });

        while (!stop) {
            Thread.onSpinWait();
        }
    }

    /**
     * Closes this resource, relinquishing any underlying resources.
     * This method is invoked automatically on objects managed by the
     * {@code try}-with-resources statement.
     *
     * @apiNote While this interface method is declared to throw {@code
     * Exception}, implementers are <em>strongly</em> encouraged to
     * declare concrete implementations of the {@code close} method to
     * throw more specific exceptions, or to throw no exception at all
     * if the close operation cannot fail.
     *
     * <p> Cases where the close operation may fail require careful
     * attention by implementers. It is strongly advised to relinquish
     * the underlying resources and to internally <em>mark</em> the
     * resource as closed, prior to throwing the exception. The {@code
     * close} method is unlikely to be invoked more than once and so
     * this ensures that the resources are released in a timely manner.
     * Furthermore, it reduces problems that could arise when the resource
     * wraps, or is wrapped, by another resource.
     *
     * <p><em>Implementers of this interface are also strongly advised
     * to not have the {@code close} method throw {@link
     * InterruptedException}.</em>
     * <p>
     * This exception interacts with a thread's interrupted status,
     * and runtime misbehavior is likely to occur if an {@code
     * InterruptedException} is {@linkplain Throwable#addSuppressed
     * suppressed}.
     * <p>
     * More generally, if it would cause problems for an
     * exception to be suppressed, the {@code AutoCloseable.close}
     * method should not throw it.
     *
     * <p>Note that unlike the {@link Closeable#close close}
     * method of {@link Closeable}, this {@code close} method
     * is <em>not</em> required to be idempotent.  In other words,
     * calling this {@code close} method more than once may have some
     * visible side effect, unlike {@code Closeable.close} which is
     * required to have no effect if called more than once.
     * <p>
     * However, implementers of this interface are strongly encouraged
     * to make their {@code close} methods idempotent.
     */
    @Override
    public void close() {
        stopServer();
    }
}
