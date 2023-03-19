package ru.borun.freedomnet.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Builder(builderMethodName = "newHttpSender")
public class HttpSender {

    private static final List<Integer> VALID_RESPONSE_CODES = List.of(200, 201, 201, 203, 204);
    private String auth;
    private String url;
    private Map<String, String> headers;
    private Map<String, String> queryParams;
    private HttpMethod method;
    private byte[] requestBody;

    public <T> T sendRequest(Class<T> bodyType)
            throws IOException, InterruptedException, InvalidHttpStatusCode {
        return new ObjectMapper().readValue(sendRequest(VALID_RESPONSE_CODES), bodyType);
    }

    public <T> T sendRequest(Class<T> bodyType, List<Integer> validResponseCodes)
            throws IOException, InterruptedException, InvalidHttpStatusCode {
        return new ObjectMapper().readValue(sendRequest(validResponseCodes), bodyType);
    }

    public byte[] sendRequest() throws InvalidHttpStatusCode, IOException, InterruptedException {
        return sendRequest(VALID_RESPONSE_CODES);
    }

    public byte[] sendRequest(List<Integer> validResponseCodes)
            throws IOException, InterruptedException, InvalidHttpStatusCode {
        var request = HttpRequest.newBuilder();

        if (this.queryParams != null && this.queryParams.size() > 0) {
            var queryParamsStr = this.queryParams.keySet().stream().map(key ->
                    String.format("%s=%s", key, this.queryParams.get(key))
            ).toList();
                request.uri(URI.create(String.format("%s?%s", url, String.join("&", queryParamsStr))));
        } else {
            request.uri(URI.create(url));
        }

        if (this.method != null) {
            switch (this.method) {
                case GET -> request.GET();
                case POST -> request.POST(HttpRequest.BodyPublishers.ofByteArray(requestBody));
            }
        } else {
            request.GET();
        }

        if (this.headers != null) {
            this.headers.keySet().forEach(key -> request.header(key, this.headers.get(key)));
        }

        if (this.auth != null) {
            request.header("Authorization", this.auth);
        }

        var response = HttpClient.newHttpClient().send(request.build(), HttpResponse.BodyHandlers.ofByteArray());
        if (!validResponseCodes.contains(response.statusCode()))
            throw new InvalidHttpStatusCode(response.statusCode(), new String(response.body(), StandardCharsets.UTF_8));
        return response.body();
    }
}