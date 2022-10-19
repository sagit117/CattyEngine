package ru.axel.catty.engine.routing;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.axel.catty.engine.headers.Headers;
import ru.axel.catty.engine.request.IHttpCattyRequest;
import ru.axel.catty.engine.response.ResponseCode;
import ru.axel.fileloader.FileLoader;

import java.net.URL;
import java.util.*;

public class Routing implements IRouting {
    private final List<ICattyRoute> routes = new ArrayList<>();

    /**
     * Метод добавляет в коллекцию маршрут.
     * @param route объект маршрута.
     */
    @Override
    public void addRoute(ICattyRoute route) {
        routes.add(route);
    }
    /**
     * Метод добавляет в коллекцию маршрут.
     * @param path путь запроса.
     * @param method метод запроса.
     * @param handler обработчик запроса.
     */
    @Override
    public void addRoute(String path, String method, RouteExecute handler) {
        routes.add(new Route(path, method, handler));
    }

    /**
     * Метод создает GET маршрут.
     * @param path маршрут.
     * @param handler обработчик маршрута.
     */
    @Override
    public void get(String path, RouteExecute handler) {
        addRoute(path, "GET", handler);
    }

    /**
     * Метод создает POST маршрут.
     * @param path маршрут.
     * @param handler обработчик маршрута.
     */
    @Override
    public void post(String path, RouteExecute handler) {
        addRoute(path, "POST", handler);
    }

    /**
     * Метод создает обработчик для статики
     * @param pathFiles путь до файлов статики
     * @param path путь запроса
     */
    @Override
    public void staticFiles(URL pathFiles, String path) {
        addRoute(path + "/*", "GET", (request, response) -> {
            var pathSplit = request.getPath().split("/");
            String fileName = pathSplit[pathSplit.length - 1];

            var file = new FileLoader(pathFiles.toString() + "/" + fileName);

            response.setResponseCode(ResponseCode.OK);
            response.addHeader(Headers.CONTENT_TYPE, file.getMineFile() + "; charset=UTF-8");
            response.addBody(file.getBytes());
        });
    }

    /**
     * Метод создает обработчик для статики из каталога ресурсов
     * @param path путь запроса, он же будет использоваться для поиска ресурсов
     */
    @Override
    public void staticResourceFiles(String path) {
        addRoute(path + "/*", "GET", (request, response) -> {
            var file = new FileLoader(
                Objects.requireNonNull(
                    Routing.class.getResource(path + request.getPath().replace(path, ""))
                )
            );

            response.setResponseCode(ResponseCode.OK);
            response.addHeader(Headers.CONTENT_TYPE, file.getMineFile() + "; charset=UTF-8");
            response.addBody(file.getBytes());
        });
    }

    /**
     * Метод возвращает маршрут подходящий под запрос.
     * @param request запрос.
     * @return маршрут подходящий под запрос или null.
     */
    @Override
    public @Nullable ICattyRoute takeRoute(@NotNull IHttpCattyRequest request) {
        return priorityRoute(getRoutesByPath(request.getPath(), request.getMethod()));
    }

    /**
     * Метод берет из массива routes паттерны и находит все подходящие маршруты к запрошенному в path
     * @param path маршрут из request
     * @return маршруты из массива routes соответствующие path
     */
    private @NotNull HashMap<String, ICattyRoute> getRoutesByPath(String path, String method) {
        HashMap<String, ICattyRoute> findUrl = new HashMap<>();               // храним результаты поиска

        for (ICattyRoute route: routes) {
            if (route.getPattern().matcher(path).matches() && Objects.equals(route.getMethod(), method)) {
                findUrl.put(route.getPattern().pattern(), route);
            }
        }

        return findUrl;
    }

    /**
     * Метод возвращает приоритетный url
     * @param findUrl найденные маршруты для отбора
     * @return приоритетный url
     */
    private @Nullable ICattyRoute priorityRoute(@NotNull HashMap<String, ICattyRoute> findUrl) {
        if (findUrl.size() == 0) return null;

        String pattern = findUrl.keySet().stream()
            .sorted(Comparator.comparingInt(String::length))
            .max(Comparator.comparingInt(
                a -> a.replaceAll("\\(\\.\\*\\)", "").length()
            )).orElseThrow();

        return findUrl.get(pattern);
    }
}
