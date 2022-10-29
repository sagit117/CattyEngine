package ru.axel.catty.engine.request;

public interface IClientInfo {
    String getLocalAddress();
    int getLocalPort();
    String getRemoteAddress();
    int getRemotePort();
}
