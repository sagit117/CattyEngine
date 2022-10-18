package ru.axel.catty.engine.routing;

import ru.axel.catty.engine.request.IHttpCattyRequest;
import ru.axel.catty.engine.response.IHttpCattyResponse;

import java.util.regex.Pattern;

public interface ICattyRoute {
    String getPath();
    Pattern getPattern();
    String getMethod();
    RouteExecute getHandler();

    /**
     * Выполнить обработку маршрута
     * @param request объект запроса
     * @param response объект ответа
     */
    void handle(IHttpCattyRequest request, IHttpCattyResponse response);
}
