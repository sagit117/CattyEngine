package ru.axel.catty.engine.routing;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.axel.catty.engine.headers.Headers;
import ru.axel.catty.engine.request.IHttpCattyRequest;
import ru.axel.catty.engine.response.ResponseCode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Routing implements IRouting {
    private final Logger logger;
    private final List<ICattyRoute> routes = new ArrayList<>();

    public Routing(Logger loggerInstance) {
        logger = loggerInstance;
    }

    /**
     * Метод добавляет в коллекцию маршрут.
     * @param route объект маршрута.
     */
    @Override
    public void addRoute(ICattyRoute route) {
        routes.add(route);
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("Добавлен маршрут. " + route.getMethod() + ":" + route.getPath());
        }
    }
    /**
     * Метод добавляет в коллекцию маршрут.
     * @param path путь запроса.
     * @param method метод запроса.
     * @param handler обработчик запроса.
     */
    @Override
    public void addRoute(String path, String method, RouteExecute handler) {
        final ICattyRoute route = new Route(path, method, handler);
        addRoute(route);
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
     * @param pathFiles путь до файлов статики(каталог)
     * @param path путь запроса
     */
    @Override
    public void staticFiles(String pathFiles, String path) {
        addRoute(path + "/*", "GET", (request, response) -> {
//            final String[] pathSplit = request.getPath().orElseThrow().split("/");
//            final String fileName = pathSplit[pathSplit.length - 1];
            final String fullPathToFile = pathFiles + request.getPath().orElseThrow();
            final Path pathToFile = Path.of(fullPathToFile.replaceFirst(path, ""));
            final String mime = Files.probeContentType(pathToFile);

            response.setResponseCode(ResponseCode.OK);
            response.addHeader(Headers.CONTENT_TYPE, mime + "; charset=utf-8");
            response.setBody(Files.readAllBytes(pathToFile));

            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Отдан статический файл: " + request.getPath().orElseThrow());
            }
        });
    }

    /**
     * Метод создает обработчик для статики из каталога ресурсов
     * @param path путь запроса, он же будет использоваться для поиска ресурсов
     */
    @Override
    public void staticResourceFiles(String path) {
        addRoute(path + "/*", "GET", (request, response) -> {
            final String pathRoute = request.getPath().orElseThrow();

            final Path pathToFile = Path.of(
                Objects.requireNonNull(
                    Routing.class.getResource(
                        pathRoute
                    )
                ).toURI()
            );
            final String mime = Files.probeContentType(pathToFile);

            response.setResponseCode(ResponseCode.OK);
            response.addHeader(Headers.CONTENT_TYPE, mime + "; charset=utf-8");
            response.setBody(Files.readAllBytes(pathToFile));

            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Отдан статический файл: " + pathRoute);
            }
        });
    }

    /**
     * Метод возвращает маршрут подходящий под запрос.
     * @param request запрос.
     * @return маршрут подходящий под запрос или null.
     */
    @Override
    public Optional<ICattyRoute> takeRoute(@NotNull IHttpCattyRequest request) {
        return request.getPath().isPresent()
            ? Optional.ofNullable(priorityRoute(getRoutesByPath(request.getPath().get(), request.getMethod())))
            : Optional.empty();
    }

    /**
     * Метод берет из массива routes паттерны и находит все подходящие маршруты к запрошенному в path
     * @param path маршрут из request
     * @return маршруты из массива routes соответствующие path
     */
    private @NotNull HashMap<String, ICattyRoute> getRoutesByPath(String path, String method) {
        final HashMap<String, ICattyRoute> findUrl = new HashMap<>(); // храним результаты поиска

        for (ICattyRoute route: routes) {
            // бывает ошибка NPE
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

        final String pattern = findUrl.keySet().stream()
            .sorted(Comparator.comparingInt(String::length))
            .max(Comparator.comparingInt(
                a -> a.replaceAll("\\(\\.\\*\\)", "").length()
            )).orElseThrow();

        return findUrl.get(pattern);
    }
}
