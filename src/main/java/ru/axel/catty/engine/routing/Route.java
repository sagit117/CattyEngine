package ru.axel.catty.engine.routing;

import org.jetbrains.annotations.NotNull;
import ru.axel.catty.engine.request.IHttpCattyRequest;
import ru.axel.catty.engine.response.IHttpCattyResponse;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Класс хранит маршрут и его обработчик
 */
public class Route implements ICattyRoute {
    private final String path;
    private final String method;
    private final RouteExecute handler;
    private final Pattern regex;

    public Route(String originalPath, String originalMethod, RouteExecute execute) {
        path = originalPath;
        method = originalMethod;
        handler = execute;
        regex = setRegex();
    }

    /**
     * Метод формирует паттерн для поиска подходящего маршрута
     *
     * @return паттерн для поиска подходящего маршрута
     */
    private @NotNull Pattern setRegex() {
        StringBuilder regexPattern = new StringBuilder();
        String[] pathSplit = path.split("/");      // делим маршрут на части

        Arrays.stream(pathSplit).forEach(value -> {
            if (value.length() != 0) {                            // если часть маршрута пустая, добавляем "/"
                regexPattern.append("/");                         // так как эта часть может прийти от запроса /
            }

            if (value.contains("*")) {
                // заменяем на паттерн поиск любых символов
                regexPattern.append(value.replace("*", "(.*)"));
            } else {
                // если запрос содержит параметры типа /{id}/
                regexPattern.append(value.replaceAll("\\{.+}", "(.*)"));
            }
        });
        if (regexPattern.length() == 0) regexPattern.append("/"); // на случай пустых значений

        return Pattern.compile(regexPattern.toString(), Pattern.CASE_INSENSITIVE);
    }

    @Override
    public String getPath() {
        return path;
    }
    @Override
    public Pattern getPattern() {
        return regex;
    }
    @Override
    public String getMethod() {
        return method;
    }
    @Override
    public RouteExecute getHandler() {
        return handler;
    }

    /**
     * Выполнить обработку маршрута
     * @param request объект запроса
     * @param response объект ответа
     */
    @Override
    public void handle(IHttpCattyRequest request, IHttpCattyResponse response) {
        handler.exec(request, response);
    }
}
