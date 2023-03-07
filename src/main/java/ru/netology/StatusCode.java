package ru.netology;

public enum StatusCode {
    OK(200),
    NOT_FOUND(404);

    public int code;
    StatusCode(int code) {
        this.code = code;
    }
}
