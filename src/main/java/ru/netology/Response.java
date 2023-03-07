package ru.netology;

public class Response {
    private final StatusCode code;
    private final String contentType;
    private final long contentLength;

    public Response(StatusCode code, String contentType, long contentLength) {
        this.code = code;
        this.contentType = contentType;
        this.contentLength = contentLength;
    }

    public StatusCode getCode() {
        return code;
    }

    public String getContentType() {
        return contentType;
    }

    public long getContentLength() {
        return contentLength;
    }
}
