package ru.netology;

public enum StatusCode {
    OK(200),
    BAD_REQUEST(400),
    NOT_FOUND(404);

    public final int code;

    StatusCode(int code) {
        this.code = code;
    }
}
