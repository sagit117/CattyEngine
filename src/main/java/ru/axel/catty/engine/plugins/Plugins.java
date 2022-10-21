package ru.axel.catty.engine.plugins;

import ru.axel.catty.engine.request.IHttpCattyRequest;
import ru.axel.catty.engine.response.IHttpCattyResponse;
import ru.axel.catty.engine.routing.RouteExecute;
import ru.axel.conveyor.Conveyor;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Logger;

/**
 * Хранилище исполняющего кода плагинов.
 */
public class Plugins extends Conveyor<String, RouteExecute> implements RouteExecute {
    private final Logger logger;
    public Plugins(Logger loggerInstance) {
        super(loggerInstance);

        logger = loggerInstance;
        this.addPipelines("parameters", new ParametersFromRoute());
    }

    /**
     * Метод последовательно обрабатывает запросы и ответы в исполняющем методе плагинов.
     * @param request объект запроса.
     * @param response объект ответа.
     */
    @Override
    public void exec(
        IHttpCattyRequest request,
        IHttpCattyResponse response
    ) {
        getPipelines().values().forEach(executor -> {
            try {
                executor.exec(request, response);
            } catch (IOException | URISyntaxException e) {
                logger.throwing(Plugins.class.getName(), "exec", e);
                e.printStackTrace();
            }
        });
    }
}
