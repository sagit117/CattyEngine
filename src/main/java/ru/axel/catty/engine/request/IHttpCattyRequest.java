package ru.axel.catty.engine.request;

import org.jetbrains.annotations.Nullable;
import ru.axel.catty.engine.headers.Headers;
import ru.axel.catty.engine.response.IHttpCattyResponse;
import ru.axel.catty.engine.routing.ICattyRoute;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

public interface IHttpCattyRequest {
    /**
     * Добавление параметров строки запроса
     * @param name имя параметра
     * @param value значение параметра
     */
    void setParams(String name, Object value);

    /**
     * Метод добавляет объект маршрута.
     * @param originalRoute объект маршрута.
     */
    void setRoute(ICattyRoute originalRoute);

    void setClientInfo(IClientInfo clientInfo);

    /**
     *  Метод выполняет обработчик маршрута, который заложен в объекте маршрута.
     * @param response объект ответа.
     */
    void handle(IHttpCattyResponse response) throws IOException, URISyntaxException;

    Optional<String> getPath();
    String getMethod();
    String getVersion();
    @Nullable String getCookie(String name);
    @Nullable String getHeaders(String name);
    @Nullable String getHeaders(Headers header);

    /**
     * Метод вернет параметр запроса, которым наполняется запрос по мере похождения через конвейер.
     * @param name имя параметра.
     * @return объект параметра запроса.
     */
    @Nullable <T> T getParams(String name);
    String getOriginalRequest();
    String getBody();
    String getQueryParam(String name);
    Optional<ICattyRoute> getRoute();

    /**
     * Метод возвращает объект данных клиента
     * @return объект данных клиента
     */
    IClientInfo getClientInfo();

    Logger getLogger();
}
