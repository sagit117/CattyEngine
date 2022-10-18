package ru.axel.catty.engine.routing;

import ru.axel.catty.engine.request.IHttpCattyRequest;
import ru.axel.catty.engine.response.IHttpCattyResponse;

/**
 * Обработки маршрута
 */
public interface RouteExecute {
    void exec(
        IHttpCattyRequest request,
        IHttpCattyResponse response
    );
}
