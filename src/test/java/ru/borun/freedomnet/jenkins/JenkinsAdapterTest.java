package ru.borun.freedomnet.jenkins;

import lombok.SneakyThrows;
import netscape.javascript.JSObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import ru.borun.freedomnet.jenkins.data.BuildData;
import ru.borun.freedomnet.jenkins.data.JobData;
import ru.borun.freedomnet.util.http.HttpSender;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

class JenkinsAdapterTest {

    private JenkinsConfig jenkinsConfig;

    private HttpSender.HttpSenderBuilder httpSenderBuilder;
    private HttpSender httpSender;
    private JenkinsAdapter jenkinsAdapter;

    @BeforeEach
    void setUp() {
        jenkinsConfig = JenkinsConfig.getInstance();
        httpSenderBuilder = Mockito.spy(HttpSender.HttpSenderBuilder.class);
        httpSender = Mockito.mock(HttpSender.class);
        assert jenkinsConfig != null;
        jenkinsAdapter = new JenkinsAdapter(jenkinsConfig);
    }

    @Test
    void testJenkinsAdapter() {
        var expectedAuth = "Basic + " + Base64.getEncoder().encodeToString(
                "%s:%s".formatted(jenkinsConfig.getUsername(), jenkinsConfig.getToken())
                        .getBytes(StandardCharsets.UTF_8)
        );
        Assertions.assertEquals(expectedAuth, jenkinsAdapter.getAuth());
    }

    @Test
    @SneakyThrows
    void testGetJobData() {
        assert jenkinsConfig != null;
        var jobUri = "test/job/uri";
        var expectedUrl = String.format("%s/%s/api/json", jenkinsConfig.getUrl(), jobUri);

        try (var staticHttpSender = Mockito.mockStatic(HttpSender.class)) {
            staticHttpSender.when(HttpSender::newHttpSender).thenReturn(httpSenderBuilder);
            Mockito.when(httpSenderBuilder.build()).thenReturn(httpSender);

            jenkinsAdapter.getJobData(jobUri);

            Mockito.verify(httpSenderBuilder, Mockito.times(1)).url(expectedUrl);
            Mockito.verify(httpSenderBuilder, Mockito.times(1)).auth(jenkinsAdapter.getAuth());
        }
    }

    @Test
    @SneakyThrows
    void testRunBuild() {
        assert jenkinsConfig != null;
        var jobUri = "test/job/uri";
        var token = "secret_token";
        var params = Map.of(
                "p1", "v1",
                "p2", "v2",
                "token", token
        );
        var expectedUrl = String.format("%s/%s/buildWithParameters", jenkinsConfig.getUrl(), jobUri);
        try (var staticHttpSender = Mockito.mockStatic(HttpSender.class)) {
            staticHttpSender.when(HttpSender::newHttpSender).thenReturn(httpSenderBuilder);
            Mockito.when(httpSenderBuilder.build()).thenReturn(httpSender);

            jenkinsAdapter.runBuild(jobUri, token, params);

            Mockito.verify(httpSenderBuilder, Mockito.times(1)).url(expectedUrl);
            Mockito.verify(httpSenderBuilder, Mockito.times(1)).queryParams(params);
            Mockito.verify(httpSenderBuilder, Mockito.times(1)).auth(jenkinsAdapter.getAuth());
        }
    }

    @Test
    @SneakyThrows
    void testUpdateBuild_buildData() {
        assert jenkinsConfig != null;
        var buildData = new BuildData();
        buildData.setUrl("http://joburl");
        var expectedUrl = String.format("%s/api/json", buildData.getUrl());
        try (var staticHttpSender = Mockito.mockStatic(HttpSender.class)) {
            staticHttpSender.when(HttpSender::newHttpSender).thenReturn(httpSenderBuilder);
            Mockito.when(httpSenderBuilder.build()).thenReturn(httpSender);

            jenkinsAdapter.updateBuild(buildData);

            Mockito.verify(httpSenderBuilder, Mockito.times(1)).url(expectedUrl);
            Mockito.verify(httpSenderBuilder, Mockito.times(1)).auth(jenkinsAdapter.getAuth());
        }
    }

    @Test
    @SneakyThrows
    void testUpdateBuild_jobUriAndBuildNumber() {
        assert jenkinsConfig != null;
        var jobUri = "test/job/uri";
        var buildNumber = 13;
        var expectedUrl = String.format("%s/%s/%d/api/json", jenkinsConfig.getUrl(), jobUri, buildNumber);
        try (var staticHttpSender = Mockito.mockStatic(HttpSender.class)) {
            staticHttpSender.when(HttpSender::newHttpSender).thenReturn(httpSenderBuilder);
            Mockito.when(httpSenderBuilder.build()).thenReturn(httpSender);

            jenkinsAdapter.updateBuild(jobUri, buildNumber);

            Mockito.verify(httpSenderBuilder, Mockito.times(1)).url(expectedUrl);
            Mockito.verify(httpSenderBuilder, Mockito.times(1)).auth(jenkinsAdapter.getAuth());
        }
    }
}