package ru.borun.freedomnet.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Log4j2
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
        log.debug("Init request with body serialization to '{}'", bodyType.getName());
        return new ObjectMapper().readValue(sendRequest(VALID_RESPONSE_CODES), bodyType);
    }

    public <T> T sendRequest(Class<T> bodyType, List<Integer> validResponseCodes)
            throws IOException, InterruptedException, InvalidHttpStatusCode {
        log.debug(
                "Init request with body serialization to '{}' and custom valid response codes '{}'",
                bodyType.getName(),
                validResponseCodes
        );
        return new ObjectMapper().readValue(sendRequest(validResponseCodes), bodyType);
    }

    public byte[] sendRequest() throws InvalidHttpStatusCode, IOException, InterruptedException {
        log.debug("Init request without body serialization");
        return sendRequest(VALID_RESPONSE_CODES);
    }

    public byte[] sendRequest(List<Integer> validResponseCodes)
            throws IOException, InterruptedException, InvalidHttpStatusCode {

        log.debug("Starting HttpRequest building...");
        var request = HttpRequest.newBuilder();

        if (queryParams != null && queryParams.size() > 0) {
            var queryParamsStr = queryParams.keySet().stream().map(key ->
                    String.format("%s=%s", key, queryParams.get(key))
            ).toList();
            var uri = URI.create(String.format("%s?%s", url, String.join("&", queryParamsStr)));
            log.debug("Setup URI to {}", uri);
            request.uri(uri);
        } else {
            var uri = URI.create(url);
            log.debug("Setup URI to {}", uri);
            request.uri(uri);
        }

        if (method != null) {
            if (method == HttpMethod.GET) {
                log.debug("Setup HTTP method to 'GET'");
                request.GET();
            } else if (method == HttpMethod.POST) {
                log.debug("Setup HTTP method to 'POST'");
                request.POST(HttpRequest.BodyPublishers.ofByteArray(requestBody));
            }
        } else {
            log.debug("The method parameter is null, using default HTTP method 'GET'");
            request.GET();
        }

        if (headers != null) {
            log.debug("Headers: {}", headers);
            headers.keySet().forEach(key -> request.header(key, headers.get(key)));
        } else {
            log.debug("There are no custom headers");
        }

        if (auth != null) {
            log.debug("Setup Authorization header to {}", auth);
            request.header("Authorization", auth);
        } else {
            log.debug("There are no authorization");
        }
        log.debug("Valid response codes: {}", validResponseCodes);
        log.debug("Sending request...");
        var response = HttpClient.newHttpClient().send(request.build(), HttpResponse.BodyHandlers.ofByteArray());
        log.debug("Response received.");
        if (!validResponseCodes.contains(response.statusCode())) {
            var responseBodyStr = new String(response.body(), StandardCharsets.UTF_8);
            log.error("Invalid response code: {}", response.statusCode());
            log.debug("Response body: {}", responseBodyStr);
            throw new InvalidHttpStatusCode(response.statusCode(), responseBodyStr);
        } else {
            log.debug("Response code is {}", response.statusCode());
        }
        return response.body();
    }
}