package ru.axel.catty.engine.headers;

public enum Headers implements IHeaders {
    DATE            ("Date"),
    CONTENT_TYPE    ("Content-Type"),
    SERVER          ("Server"),
    CONNECTION      ("Connection"),
    CONTENT_LENGTH  ("Content-Length"),
    ACCEPT_ENCODING ("Accept-Encoding"),
    CONTENT_ENCODING("Content-encoding"),
    SET_COOKIE      ("Set-Cookie"),
    ALLOW           ("Allow"),
    LOCATION        ("Location"),
    KEEP_ALIVE      ("Keep-Alive"),
    ;

    private final String headerName;

    Headers(String headerName) {
        this.headerName = headerName;
    }

    @Override
    final public String getHeaderName() {
        return headerName;
    }

    @Override
    final public String toString() {
        return getHeaderName();
    }
}
