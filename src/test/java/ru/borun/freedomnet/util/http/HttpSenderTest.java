package ru.borun.freedomnet.util.http;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Test HTTP sender")
class HttpSenderTest {

    @Test
    @DisplayName("Test with body serialization")
    @SneakyThrows
    void testSendRequest_bodySerialization() {
        var httpSenderMock = mock(HttpSender.class);
        var testResponse = "{\"test\": \"qwe123\"}".getBytes(StandardCharsets.UTF_8);
        var expectedResult = Map.of("test", "qwe123");
        when(httpSenderMock.sendRequest(HttpSender.VALID_RESPONSE_CODES)).thenReturn(testResponse);
        when(httpSenderMock.sendRequest(Map.class)).thenCallRealMethod();
        assertEquals(expectedResult, httpSenderMock.sendRequest(Map.class));
    }

    @Test
    @DisplayName("Test with body serialization and custom response codes")
    @SneakyThrows
    void testSendRequest_bodySerializationAndCustomRespCodes() {
        var httpSenderMock = mock(HttpSender.class);
        var testResponse = "{\"test\": \"qwe123\"}".getBytes(StandardCharsets.UTF_8);
        var expectedResult = Map.of("test", "qwe123");
        var expectedResponseCodes = List.of(200, 300, 400, 500);
        when(httpSenderMock.sendRequest(expectedResponseCodes)).thenReturn(testResponse);
        when(httpSenderMock.sendRequest(Map.class, expectedResponseCodes)).thenCallRealMethod();
        assertEquals(expectedResult, httpSenderMock.sendRequest(Map.class, expectedResponseCodes));
    }

    @Test
    @DisplayName("Test with no input arguments")
    @SneakyThrows
    void testSendRequest_noArgs() {
        var httpSenderMock = mock(HttpSender.class);
        var testResponse = "{\"test\": \"qwe123\"}".getBytes(StandardCharsets.UTF_8);
        when(httpSenderMock.sendRequest(HttpSender.VALID_RESPONSE_CODES)).thenReturn(testResponse);
        when(httpSenderMock.sendRequest()).thenCallRealMethod();
        assertArrayEquals(testResponse, httpSenderMock.sendRequest());
    }

    private void testWithMockedHttpClient(
            HttpRequest expectedHttpRequest,
            HttpSender httpSender,
            HttpResponse<byte[]> httpResponse) throws IOException, InterruptedException, InvalidHttpStatusCode {
        var httpClientMock = mock(HttpClient.class);
        var httpReqCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        try (var staticHttpClient = mockStatic(HttpClient.class)) {
            staticHttpClient.when(HttpClient::newHttpClient).thenReturn(httpClientMock);
            when(httpClientMock.send(httpReqCaptor.capture(), eq(HttpResponse.BodyHandlers.ofByteArray())))
                    .thenReturn(httpResponse);
            httpSender.sendRequest(HttpSender.VALID_RESPONSE_CODES);
            assertEquals(expectedHttpRequest.uri().getScheme(), httpReqCaptor.getValue().uri().getScheme());
            assertEquals(expectedHttpRequest.uri().getPath(), httpReqCaptor.getValue().uri().getPath());
            if (expectedHttpRequest.uri().getQuery() != null) {
                assertEquals(
                        toQueryMap(expectedHttpRequest.uri().getQuery()),
                        toQueryMap(httpReqCaptor.getValue().uri().getQuery())
                );
            }
            assertEquals(expectedHttpRequest.headers(), httpReqCaptor.getValue().headers());
            assertEquals(expectedHttpRequest.method(), httpReqCaptor.getValue().method());
            expectedHttpRequest.bodyPublisher().ifPresent(value ->
                    assertEquals(
                            value.contentLength(),
                            httpReqCaptor.getValue().bodyPublisher().get().contentLength()
                    )
            );
        }
    }

    private Map<String, String> toQueryMap(String query) {
        var queryMap = new LinkedHashMap<String, String>();
        Arrays.stream(query.split("&")).forEach(pair -> {
            var splitPair = pair.split("=");
            queryMap.put(splitPair[0], splitPair[1]);
        });
        return queryMap;
    }

    @Test
    @DisplayName("Test with no input query parameters, headers, auth and method")
    @SneakyThrows
    void testSendRequest_noQuery_noHeaders_noAuth_noMethod() {
        var url = "https://google.com/";
        var expectedHttpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        var httpSender = HttpSender.newHttpSender()
                .url(url)
                .method(HttpMethods.GET)
                .build();
        var httpResponse = new TestHttpResponse("test".getBytes(StandardCharsets.UTF_8), 200);
        testWithMockedHttpClient(expectedHttpRequest, httpSender, httpResponse);
    }

    @Test
    @DisplayName("Test with input 'GET' method and no headers, no query parameters and no auth")
    @SneakyThrows
    void testSendRequest_noQuery_noHeaders_noAuth_forceGetMethod() {
        var url = "https://google.com/";
        var expectedHttpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        var httpSender = HttpSender.newHttpSender()
                .url(url)
                .build();
        var httpResponse = new TestHttpResponse("test".getBytes(StandardCharsets.UTF_8), 200);
        testWithMockedHttpClient(expectedHttpRequest, httpSender, httpResponse);
    }

    @Test
    @DisplayName("Test with query parameters headers, auth and no input method")
    @SneakyThrows
    void testSendRequest_withQuery_withHeaders_withAuth_withMethod() {
        var url = "https://ya.ru/";
        var auth = "Basic 0L/Rg9GC0LjQvTrRhdGD0LnQu9C+Cg==";
        var body = "test body".getBytes(StandardCharsets.UTF_8);
        var expectedHttpRequest = HttpRequest.newBuilder()
                .uri(UriBuilder
                        .fromUri(url)
                        .queryParam("q1", "v1")
                        .queryParam("q2", "v2")
                        .build()
                )
                .header("h1", "v11")
                .header("h2", "v22")
                .header("Authorization", auth)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();
        var httpSender = HttpSender.newHttpSender()
                .url(url)
                .auth("Basic 0L/Rg9GC0LjQvTrRhdGD0LnQu9C+Cg==")
                .queryParams(Map.of("q1", "v1", "q2", "v2"))
                .headers(Map.of("h1", "v11", "h2", "v22"))
                .requestBody(body)
                .method(HttpMethods.POST)
                .build();
        var httpResponse = new TestHttpResponse("test".getBytes(StandardCharsets.UTF_8), 200);
        testWithMockedHttpClient(expectedHttpRequest, httpSender, httpResponse);
    }

    @Test
    @DisplayName("Test if returned invalid response code")
    @SneakyThrows
    void testSendRequest_invalidResponseCode() {
        var url = "https://microsoft.com/";
        var expectedHttpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        var httpSender = HttpSender.newHttpSender()
                .url(url)
                .build();
        var httpResponse = new TestHttpResponse("test".getBytes(StandardCharsets.UTF_8), 500);
        assertThrows(InvalidHttpStatusCode.class, () ->
            testWithMockedHttpClient(expectedHttpRequest, httpSender, httpResponse)
        );
    }
}