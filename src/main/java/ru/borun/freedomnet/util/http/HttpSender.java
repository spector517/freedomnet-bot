package ru.borun.freedomnet.util.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.http.client.utils.URIBuilder;

import javax.ws.rs.core.UriBuilder;
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

    public static final List<Integer> VALID_RESPONSE_CODES = List.of(200, 201, 201, 203, 204);
    private String auth;
    private String url;
    private Map<String, String> headers;
    private Map<String, String> queryParams;
    private HttpMethods method;
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
        var requestBuilder = HttpRequest.newBuilder();
        processQueryParams(requestBuilder);
        processMethod(requestBuilder);
        processHeaders(requestBuilder);
        processAuth(requestBuilder);

        log.debug("Valid response codes: {}", validResponseCodes);
        log.debug("Sending request...");
        var response = HttpClient.newHttpClient().send(requestBuilder.build(), HttpResponse.BodyHandlers.ofByteArray());
        log.debug("Response received.");

        return processResponse(validResponseCodes, response);
    }

    private void processQueryParams(HttpRequest.Builder requestBuilder) {
        if (queryParams != null && queryParams.size() > 0) {
            var uriBuilder = UriBuilder.fromUri(url);
            queryParams.forEach(uriBuilder::queryParam);
            var uri = uriBuilder.build();
            log.debug("Setup URI to {}", uri);
            requestBuilder.uri(uri);
        } else {
            var uri = URI.create(url);
            log.debug("Setup URI to {}", uri);
            requestBuilder.uri(uri);
        }
    }

    private void processMethod(HttpRequest.Builder requestBuilder) {
        if (method != null) {
            if (method == HttpMethods.GET) {
                log.debug("Setup HTTP method to 'GET'");
                requestBuilder.GET();
            } else if (method == HttpMethods.POST) {
                log.debug("Setup HTTP method to 'POST'");
                requestBuilder.POST(HttpRequest.BodyPublishers.ofByteArray(requestBody));
            }
        } else {
            log.debug("The method parameter is null, using default HTTP method 'GET'");
            requestBuilder.GET();
        }
    }

    private void processHeaders(HttpRequest.Builder requestBuilder) {
        if (headers != null) {
            log.debug("Headers: {}", headers);
            headers.keySet().forEach(key -> requestBuilder.header(key, headers.get(key)));
        } else {
            log.debug("There are no custom headers");
        }
    }

    private void processAuth(HttpRequest.Builder requestBuilder) {
        if (auth != null) {
            log.debug("Setup Authorization header to {}", auth);
            requestBuilder.header("Authorization", auth);
        } else {
            log.debug("There are no authorization");
        }
    }

    private byte[] processResponse(List<Integer> validResponseCodes, HttpResponse<byte[]> response)
            throws InvalidHttpStatusCode {
        if (!validResponseCodes.contains(response.statusCode())) {
            var responseBodyStr = new String(response.body(), StandardCharsets.UTF_8);
            log.error("Invalid response code: {}", response.statusCode());
            log.error("Response body: {}", responseBodyStr);
            throw new InvalidHttpStatusCode(response.statusCode(), responseBodyStr);
        } else {
            log.debug("Response code is {}", response.statusCode());
        }
        return response.body();
    }
}