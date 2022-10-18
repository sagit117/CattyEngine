package ru.axel.catty.engine.request;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.axel.logger.MiniLogger;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Класс содержит данные запроса.
 */
public final class Request implements IHttpCattyRequest {
    private static final Logger logger = MiniLogger.getLogger(Request.class);
    private final HashMap<String, String> headers = new HashMap<>();
    private final HashMap<String, String> cookie = new HashMap<>();
    private final HashMap<String, String> params = new HashMap<>();         // параметры из пути маршрута
    private final HashMap<String, String> queryParams = new HashMap<>();    // параметры запроса
    private final String originalRequest;
    private String body;
    private String method;
    public String path;
    private String version;

    /**
     * Создание экземпляра.
     * @param request строка переданная клиентом.
     */
    public Request(@NotNull String request) throws RequestBuildException {
        originalRequest = request;
        rawRequest(originalRequest);
    }
    public Request(@NotNull ByteBuffer request) throws RequestBuildException {
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
        var lines = request.lines().toList();
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

            var lineSplit = line.split(":");
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
        logger.severe("StartLine: " + line);
        String[] starts = line.split(" ");
        if (starts.length != 3) throw new RequestBuildException("Bad start line");

        method = starts[0];
        setPath(starts[1]);
        version = starts[2];

        logger.severe("Запрос создан. StartLine: " + String.join(", ", starts));
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
        logger.severe("CookieRaw: " + cookieRaw);
        var cookieSplit = cookieRaw.split(";");

        Arrays.stream(cookieSplit).forEach(line -> {
            var splitLine = line.split("=");
            cookie.put(splitLine[0].trim(), splitLine.length == 1 ? "" : splitLine[1]);
        });
    }

    @Contract(mutates = "this")
    private void setPath(@NotNull String pathRequest) {
        var queryParam = pathRequest.split("\\?");
        path = queryParam[0];

        if (queryParam.length > 1) setQueryParams(queryParam[1]);
    }

    private void setQueryParams(@NotNull String params) {
        var paramsString = params.split("&");

        for(String param : paramsString) {
            var pairParam = param.split("=");
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
    public String getCookie(String name) {
        return cookie.getOrDefault(name, null);
    }
    @Override
    public String getHeaders(String name) {
        return headers.getOrDefault(name, null);
    }
    @Override
    public String getParams(String name) {
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
}
