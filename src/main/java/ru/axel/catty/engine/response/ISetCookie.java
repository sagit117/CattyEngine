package ru.axel.catty.engine.response;

import org.jetbrains.annotations.NotNull;

import java.util.Date;

public interface ISetCookie {
    SetCookie setExpires(@NotNull Date date);
    SetCookie setMaxAge(int age);
    SetCookie setPath(String path);
    SetCookie setDomain(String domain);
    SetCookie setSecure(Boolean secure);
    SetCookie setHttpOnly(Boolean httpOnly);
    SetCookie setSameSite(ISameSite sameSite);

    @Override
    String toString();
}
