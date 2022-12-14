package ru.axel.catty.engine.routing;

import ru.axel.catty.engine.request.IHttpCattyRequest;
import ru.axel.catty.engine.response.IHttpCattyResponse;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

/**
 * Интерфейс маршрута
 */
public interface ICattyRoute {
    String getPath();

    /**
     * Метод возвращает паттерн для поиска подходящего маршрута.
     * @return паттерн для поиска подходящего маршрута.
     */
    Pattern getPattern();
    String getMethod();
    RouteExecute getHandler();

    /**
     * Выполнить обработку маршрута
     * @param request объект запроса
     * @param response объект ответа
     * @throws IOException ошибка чтения статического файла.
     * @throws URISyntaxException ошибка формирования URL при чтении статического файла.
     * @throws NullPointerException не удалось получить статический файл.
     */
    void handle(
        IHttpCattyRequest request,
        IHttpCattyResponse response
    ) throws IOException, URISyntaxException, NullPointerException;
}
