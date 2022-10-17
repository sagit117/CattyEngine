package ru.axel.catty.engine.request;

public interface IHttpCattyRequest {
    String getPath();
    String getMethod();
    String getVersion();
    String getCookie(String name);
    String getHeaders(String name);
    String getParams(String name);
    String getOriginalRequest();
    String getBody();
    String getQueryParam(String name);
}
