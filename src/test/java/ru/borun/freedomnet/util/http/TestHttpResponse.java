package ru.borun.freedomnet.util.http;

import lombok.RequiredArgsConstructor;

import javax.net.ssl.SSLSession;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

@RequiredArgsConstructor
public class TestHttpResponse implements HttpResponse<byte[]> {

    private final byte[] body;
    private final int status;

    @Override
    public int statusCode() {
        return status;
    }

    @Override
    public HttpRequest request() {
        return null;
    }

    @Override
    public Optional<HttpResponse<byte[]>> previousResponse() {
        return Optional.empty();
    }

    @Override
    public HttpHeaders headers() {
        return null;
    }

    @Override
    public byte[] body() {
        return body;
    }

    @Override
    public Optional<SSLSession> sslSession() {
        return Optional.empty();
    }

    @Override
    public URI uri() {
        return null;
    }

    @Override
    public HttpClient.Version version() {
        return null;
    }
}
