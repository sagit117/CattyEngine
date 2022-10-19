package ru.axel.catty.engine.utilites;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Вспомогательный класс с regex паттернами для тела запроса.
 */
public final class RegexPatterns {
    public static @NotNull Matcher boundaryFinished(String boundary, String str) {
        final Pattern pattern = Pattern.compile("--" + boundary + "--");
        return pattern.matcher(str);
    }
    public static @NotNull Matcher contentLength(String str) {
        final Pattern pattern = Pattern.compile("Content-Length: (\\d+)");
        return pattern.matcher(str);
    }
    public static @NotNull Matcher boundary(String str) {
        final Pattern pattern = Pattern.compile("boundary=(.*)\r\n");
        return pattern.matcher(str);
    }
}
