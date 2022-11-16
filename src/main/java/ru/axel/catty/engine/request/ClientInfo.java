package ru.axel.catty.engine.request;

import ru.axel.catty.engine.utilites.RegexPatterns;

import java.io.Serializable;
import java.net.SocketAddress;
import java.util.regex.Matcher;

import static java.lang.Integer.parseInt;

public class ClientInfo implements IClientInfo, Serializable {
    private final SocketAddress local;
    private final SocketAddress remote;

    public ClientInfo(SocketAddress localAddress, SocketAddress remoteAddress) {
        local = localAddress;
        remote = remoteAddress;
    }


    @Override
    public String getLocalAddress() {
        final Matcher matcher = RegexPatterns.getAddressFromSocketAddress(local);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return "";
    }

    @Override
    public int getLocalPort() {
        final Matcher matcher = RegexPatterns.getAddressFromSocketAddress(local);

        if (matcher.find()) {
            return parseInt(matcher.group(2));
        }

        return -1;
    }

    @Override
    public String getRemoteAddress() {
        final Matcher matcher = RegexPatterns.getAddressFromSocketAddress(remote);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return "";
    }

    @Override
    public int getRemotePort() {
        final Matcher matcher = RegexPatterns.getAddressFromSocketAddress(remote);

        if (matcher.find()) {
            return parseInt(matcher.group(2));
        }

        return -1;
    }

    @Override
    public String toString() {
        return "{ local: \"" + local + "\", remote: \"" + remote + "\" }";
    }
}
