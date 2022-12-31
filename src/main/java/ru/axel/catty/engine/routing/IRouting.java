package ru.axel.catty.engine.routing;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.axel.catty.engine.request.IHttpCattyRequest;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

public interface IRouting {
    /**
     * Метод добавляет в коллекцию маршрут.
     * @param route объект маршрута.
     */
    void addRoute(ICattyRoute route);

    /**
     * Метод добавляет в коллекцию маршрут.
     * @param path путь запроса.
     * @param method метод запроса.
     * @param handler обработчик запроса.
     */
    void addRoute(String path, String method, RouteExecute handler);

    /**
     * Метод создает GET маршрут.
     * @param path маршрут.
     * @param handler обработчик маршрута.
     */
    void get(String path, RouteExecute handler);

    /**
     * Метод создает POST маршрут.
     * @param path маршрут.
     * @param handler обработчик маршрута.
     */
    void post(String path, RouteExecute handler);

    /**
     * Метод создает обработчик для статики
     * @param pathFiles путь до файлов статики
     * @param path путь запроса
     */
    void staticFiles(String pathFiles, String path);

    /**
     * Метод создает обработчик для статики из каталога ресурсов
     * @param path путь запроса, он же будет использоваться для поиска ресурсов
     */
    void staticResourceFiles(String path);

    /**
     * Метод возвращает маршрут подходящий под запрос и присваивает его объекту запроса.
     * @param request запрос.
     * @return маршрут подходящий под запрос.
     */
    Optional<ICattyRoute> takeRoute(@NotNull IHttpCattyRequest request);
}
