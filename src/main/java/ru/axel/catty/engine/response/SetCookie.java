package ru.axel.catty.engine.response;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.Optional;

public class SetCookie implements ISetCookie {
    private final String nameCookie;
    private final String valueCookie;
    private Date expires;
    private int maxAge; // sec
    private String pathCookie;
    private String domainCookie;
    private Boolean isSecureCookie = false;
    private Boolean isHttpOnly = false;
    private ISameSite sameSiteCookie = SameSite.LAX;

    public SetCookie(String name, String value) {
        nameCookie = name;
        valueCookie = value;
    }

    /**
     * Установка срока действия куки.
     * @param date дата окончания
     * @return объект хранения куки.
     */
    @Override
    public SetCookie setExpires(@NotNull Date date) {
        expires = date;
        return this;
    }

    /**
     * Установить максимальный срок действия куки.
     * @param age вреия в секундах.
     * @return объект хранения куки.
     */
    @Override
    public SetCookie setMaxAge(int age) {
        maxAge = age;
        return this;
    }
    @Override
    public SetCookie setPath(String path) {
        pathCookie = path;
        return this;
    }
    @Override
    public SetCookie setDomain(String domain) {
        domainCookie = domain;
        return this;
    }
    @Override
    public SetCookie setSecure(Boolean secure) {
        isSecureCookie = secure;
        return this;
    }
    @Override
    public SetCookie setHttpOnly(Boolean httpOnly) {
        isHttpOnly = httpOnly;
        return this;
    }
    @Override
    public SetCookie setSameSite(ISameSite sameSite) {
        sameSiteCookie = sameSite;
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder cookieString = new StringBuilder();
        cookieString.append(nameCookie).append("=").append(valueCookie);

        if (maxAge > 0) {
            cookieString.append("; Max-Age=").append(maxAge);
        } else {
            Optional.ofNullable(expires).ifPresent((date) -> {
                cookieString.append("; Expires=").append(date);
            });
        }

        Optional.ofNullable(pathCookie).ifPresent((path) -> {
            cookieString.append("; Path=").append(path);
        });

        Optional.ofNullable(domainCookie).ifPresent((domain) -> {
            cookieString.append("; Domain=").append(domain);
        });

        cookieString.append("; SameSite=").append(sameSiteCookie);

        if (isSecureCookie || sameSiteCookie == SameSite.NONE) {
            cookieString.append("; Secure");
        }

        if (isHttpOnly) cookieString.append("; HttpOnly");

        return cookieString.toString();
    }
}
