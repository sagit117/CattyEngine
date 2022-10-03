package ru.axel.catty.engine;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.axel.creator.instances.CreateInstanceException;
import ru.axel.creator.instances.CreatorInstances;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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

/**
 * Движок сервера, создает подключение и слушает указанный порт.
 * Движок ничего не знает об обработки ответа или запроса, за это отвечает класс в параметрах конструктора handler.
 */
public final class CattyEngine implements ICattyEngine {
    private static final Logger logger = LoggerFactory.getLogger(CattyEngine.class);
    private final InetSocketAddress hostAddress;
    private final ExecutorService pool;
    private final int buffer_size = 16_384; // 16kb
    private volatile boolean stop = false;
    private final Class<? extends EngineHandlerImpl> engineHandler;
    private final int limitAllocateBufferForRequest; // максимальный размер буфера для принятия запроса

    /**
     * Конструктор класса
     * @param hostAddress адрес сервера
     * @param poolLimit лимит потоков
     * @param limitAllocateBufferForRequest максимальный размер буфера для принятия запроса
     * @param handler класс обработчик запроса и ответа
     * @throws IOException ошибка запуска сервера
     */
    @SuppressWarnings("unchecked")
    public CattyEngine(
            InetSocketAddress hostAddress,
            int poolLimit,
            int limitAllocateBufferForRequest,
            @NotNull Class<? extends EngineHandlerImpl> handler
    ) throws IOException {
        this.hostAddress = hostAddress;
        pool = Executors.newWorkStealingPool(poolLimit);
        this.limitAllocateBufferForRequest = limitAllocateBufferForRequest;
        engineHandler = handler;

        startServer();
    }

    /**
     * Метод создает подключение
     * @throws IOException ошибка подключения
     */
    @Override
    public void startServer() throws IOException {
        AsynchronousChannelGroup group = AsynchronousChannelGroup.withThreadPool(pool);
        AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open(group);
        server.bind(hostAddress);

        logger.info("Сервер запущен на порту: " + hostAddress.getPort());
        loop(server);
    }

    /**
     * Метод запускает петлю обработки событий
     */
    @Override
    public void loop(@NotNull AsynchronousServerSocketChannel server) {
        server.accept(null, new CompletionHandler<>() {
            @Override
            public void completed(AsynchronousSocketChannel client, Object attachment) {
                if (server.isOpen()) {
                    server.accept(null, this);
                }

                if (client != null && client.isOpen()) {
                    EngineHandlerImpl handler;
                    try {
                        handler = CreatorInstances
                                .createInstance(engineHandler, client, limitAllocateBufferForRequest);
                    } catch (
                            InvocationTargetException
                            | InstantiationException
                            | IllegalAccessException
                            | CreateInstanceException e
                    ) {
                        throw new RuntimeException(e);
                    }

                    ByteBuffer buffer = ByteBuffer.allocate(buffer_size);

                    Map<String, Object> readInfo = new HashMap<>();
                    readInfo.put("action", "read");
                    readInfo.put("buffer", buffer);

                    client.read(buffer, readInfo, handler);
                }
            }

            @Override
            public void failed(Throwable ex, Object attachment) {
                logger.error("Ошибка принятия соединения от клиента. Инфо: " + attachment);
                ex.printStackTrace();
            }
        });

        while (!stop) {
            Thread.onSpinWait();
        }
    }

    /**
     * Метод останавливает сервер
     */
    @Override
    public void stopServer() {
        stop = true;
    }
}
