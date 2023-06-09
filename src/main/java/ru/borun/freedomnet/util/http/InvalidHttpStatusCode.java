package ru.borun.freedomnet.util.http;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class InvalidHttpStatusCode extends Exception {

    private final int statusCode;
    private final String body;

    @Override
    public String toString() {
        return "Invalid status code: %d%n%s".formatted(statusCode, body);
    }
}