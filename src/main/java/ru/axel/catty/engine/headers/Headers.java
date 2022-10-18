package ru.axel.catty.engine.headers;

public enum Headers {
    DATE            ("Date"),
    CONTENT_TYPE    ("Content-Type"),
    SERVER          ("Server"),
    CONNECTION      ("Connection"),
    CONTENT_LENGTH  ("Content-Length"),
    ACCEPT_ENCODING ("Accept-Encoding"),
    CONTENT_ENCODING("Content-encoding");

    private final String headerName;

    Headers(String headerName) {
        this.headerName = headerName;
    }

    public String getHeaderName() {
        return headerName;
    }
}
