package ru.axel.catty.engine.routing;

import ru.axel.catty.engine.request.IHttpCattyRequest;
import ru.axel.catty.engine.response.IHttpCattyResponse;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

/**
 * Обработки маршрута
 */
public interface RouteExecute {
    void exec(
        IHttpCattyRequest request,
        IHttpCattyResponse response
    ) throws IOException, URISyntaxException;
}
