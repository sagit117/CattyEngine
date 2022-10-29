package ru.axel.catty.engine.request;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.axel.catty.engine.response.IHttpCattyResponse;
import ru.axel.catty.engine.routing.ICattyRoute;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Класс содержит данные запроса.
 */
public final class Request implements IHttpCattyRequest {
    private final Logger logger;
    private final HashMap<String, String> headers = new HashMap<>();
    private final HashMap<String, String> cookie = new HashMap<>();
    private final HashMap<String, String> params = new HashMap<>();         // параметры из пути маршрута
    private final HashMap<String, String> queryParams = new HashMap<>();    // параметры запроса
    private final String originalRequest;
    private String body;
    private String method;
    private String path;
    private String version;
    private ICattyRoute route;
    private IClientInfo client;

    /**
     * Создание экземпляра.
     * @param request строка переданная клиентом.
     */
    public Request(@NotNull String request, Logger loggerInstance) throws RequestBuildException {
        logger = loggerInstance;
        originalRequest = request;
        rawRequest(originalRequest);
    }
    public Request(@NotNull ByteBuffer request, Logger loggerInstance) throws RequestBuildException {
        logger = loggerInstance;
        originalRequest = new String(request.array()).trim();
        rawRequest(originalRequest);
    }

    /**
     * Устанавливает заголовки запроса и передает данные в cookie.
     * Устанавливает тело запроса.
     * @param request строка запроса.
     * @throws RequestBuildException ошибка создания запроса.
     */
    private void rawRequest(@NotNull String request) throws RequestBuildException {
        final var lines = request.lines().toList();
        int index = -1;

        for (String line: lines) {
            index++;

            if (index == 0) {
                setStartLine(line);
                continue;
            }
            if (line.isEmpty()) {
                setBody(lines.stream().skip(index));
                break;
            }

            final var lineSplit = line.split(":");
            headers.put(lineSplit[0].trim(), lineSplit[1].trim());

            if (lineSplit[0].toLowerCase(Locale.ROOT).equals("cookie")) setCookie(lineSplit[1]);
        }
    }

    /**
     * Разбор start line запроса.
     * @param line первая строка запроса.
     * @throws RequestBuildException если запрос не соответствует шаблону "Method Path Version".
     */
    private void setStartLine(@NotNull String line) throws RequestBuildException {
        logger.finest("StartLine: " + line);

        final String[] starts = line.split(" ");
        if (starts.length != 3) throw new RequestBuildException("Bad start line");

        method = starts[0];
        setPath(starts[1]);
        version = starts[2];

        logger.finest("Запрос создан. StartLine: " + String.join(", ", starts));
    }

    /**
     * Метод устанавливает тело запроса.
     * @param lines стрим со строками тела запроса.
     */
    private void setBody(@NotNull Stream<String> lines) {
        body = lines.collect(Collectors.joining());
    }

    /**
     * Создает список cookie.
     * @param cookieRaw запрос из заголовка cookie.
     */
    private void setCookie(@NotNull String cookieRaw) {
        logger.finest("CookieRaw: " + cookieRaw);

        final var cookieSplit = cookieRaw.split(";");

        Arrays.stream(cookieSplit).forEach(line -> {
            final var splitLine = line.split("=");
            cookie.put(splitLine[0].trim(), splitLine.length == 1 ? "" : splitLine[1]);
        });
    }

    @Contract(mutates = "this")
    private void setPath(@NotNull String pathRequest) {
        final var queryParam = pathRequest.split("\\?");
        path = queryParam[0];

        if (queryParam.length > 1) setQueryParams(queryParam[1]);
    }

    private void setQueryParams(@NotNull String params) {
        final var paramsString = params.split("&");

        for(String param : paramsString) {
            final var pairParam = param.split("=");
            queryParams.put(pairParam[0], pairParam.length > 1 ? pairParam[1] : null);
        }
    }

    /**
     * Добавление параметров строки запроса
     * @param name имя параметра
     * @param value значение параметра
     */
    @Override
    public void setParams(String name, String value) {
        params.put(name, value);
    }

    /**
     * Метод добавляет объект маршрута.
     * @param originalRoute объект маршрута.
     */
    @Override
    public void setRoute(ICattyRoute originalRoute) {
        route = originalRoute;
    }

    @Override
    public void setClientInfo(IClientInfo clientInfo) {
        client = clientInfo;
    }

    /**
     * Метод выполняет обработчик маршрута, который заложен в объекте маршрута.
     *
     * @param response объект ответа.
     */
    @Override
    public void handle(IHttpCattyResponse response) throws IOException, URISyntaxException {
        getRoute().orElseThrow().handle(this, response);
    }

    @Override
    public String getPath() {
        return path;
    }
    @Override
    public String getMethod() {
        return method;
    }
    @Override
    public String getVersion() {
        return version;
    }
    @Override
    public @Nullable String getCookie(String name) {
        return cookie.getOrDefault(name, null);
    }
    @Override
    public @Nullable String getHeaders(String name) {
        return headers.getOrDefault(name, null);
    }
    @Override
    public @Nullable String getParams(String name) {
        return params.getOrDefault(name, null);
    }
    @Override
    public String getOriginalRequest() {
        return originalRequest;
    }
    @Override
    public String getBody() {
        return body;
    }
    @Override
    public String getQueryParam(String name) {
        return queryParams.get(name);
    }
    @Override
    public Optional<ICattyRoute> getRoute() {
        return Optional.ofNullable(route);
    }
    /**
     * Метод возвращает объект данных клиента
     *
     * @return объект данных клиента
     */
    @Override
    public IClientInfo getClientInfo() {
        return client;
    }
}
