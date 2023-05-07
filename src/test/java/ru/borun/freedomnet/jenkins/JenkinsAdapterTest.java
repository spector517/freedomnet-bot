package ru.borun.freedomnet.jenkins;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.borun.freedomnet.jenkins.data.ArtifactData;
import ru.borun.freedomnet.jenkins.data.BuildData;
import ru.borun.freedomnet.util.http.HttpSender;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@DisplayName("Test Jenkins adapter")
class JenkinsAdapterTest {

    private JenkinsConfig jenkinsConfig;

    private HttpSender.HttpSenderBuilder httpSenderBuilder;
    private HttpSender httpSender;
    private JenkinsAdapter jenkinsAdapter;

    @BeforeEach
    void setUp() {
        jenkinsConfig = JenkinsConfig.getInstance();
        httpSenderBuilder = spy(HttpSender.HttpSenderBuilder.class);
        httpSender = mock(HttpSender.class);
        assert jenkinsConfig != null;
        jenkinsAdapter = new JenkinsAdapter(jenkinsConfig);
    }

    @Test
    @DisplayName("Test constructor")
    void testJenkinsAdapter() {
        var expectedAuth = "Basic + " + Base64.getEncoder().encodeToString(
                "%s:%s".formatted(jenkinsConfig.getUsername(), jenkinsConfig.getToken())
                        .getBytes(StandardCharsets.UTF_8)
        );
        assertEquals(expectedAuth, jenkinsAdapter.getAuth());
    }

    @Test
    @DisplayName("Test get job data from Jenkins")
    @SneakyThrows
    void testGetJobData() {
        assert jenkinsConfig != null;
        var jobUri = "test/job/uri";
        var expectedUrl = "%s/%s/api/json".formatted(jenkinsConfig.getUrl(), jobUri);

        try (var staticHttpSender = mockStatic(HttpSender.class)) {
            staticHttpSender.when(HttpSender::newHttpSender).thenReturn(httpSenderBuilder);
            when(httpSenderBuilder.build()).thenReturn(httpSender);

            jenkinsAdapter.getJobData(jobUri);

            verify(httpSenderBuilder, times(1)).url(expectedUrl);
            verify(httpSenderBuilder, times(1)).auth(jenkinsAdapter.getAuth());
        }
    }

    @Test
    @DisplayName("Test run build")
    @SneakyThrows
    void testRunBuild() {
        assert jenkinsConfig != null;
        var jobUri = "test/job/uri";
        var token = "secret_token";
        var params = Map.of("p1", "v1", "p2", "v2", "token", token);
        var expectedUrl = "%s/%s/buildWithParameters".formatted(jenkinsConfig.getUrl(), jobUri);
        try (var staticHttpSender = mockStatic(HttpSender.class)) {
            staticHttpSender.when(HttpSender::newHttpSender).thenReturn(httpSenderBuilder);
            when(httpSenderBuilder.build()).thenReturn(httpSender);

            jenkinsAdapter.runBuild(jobUri, token, params);

            verify(httpSenderBuilder, times(1)).url(expectedUrl);
            verify(httpSenderBuilder, times(1)).queryParams(params);
            verify(httpSenderBuilder, times(1)).auth(jenkinsAdapter.getAuth());
        }
    }

    @Test
    @DisplayName("Test update build data from Jenkins by BuildData")
    @SneakyThrows
    void testUpdateBuild_buildData() {
        assert jenkinsConfig != null;
        var buildData = new BuildData();
        buildData.setUrl("http://joburl");
        var expectedUrl = "%s/api/json".formatted(buildData.getUrl());
        try (var staticHttpSender = mockStatic(HttpSender.class)) {
            staticHttpSender.when(HttpSender::newHttpSender).thenReturn(httpSenderBuilder);
            when(httpSenderBuilder.build()).thenReturn(httpSender);

            jenkinsAdapter.updateBuild(buildData);

            verify(httpSenderBuilder, times(1)).url(expectedUrl);
            verify(httpSenderBuilder, times(1)).auth(jenkinsAdapter.getAuth());
        }
    }

    @Test
    @DisplayName("Test update build data from Jenkins by job uri and build number")
    @SneakyThrows
    void testUpdateBuild_jobUriAndBuildNumber() {
        assert jenkinsConfig != null;
        var jobUri = "test/job/uri";
        var buildNumber = 13;
        var expectedUrl = "%s/%s/%d/api/json".formatted(jenkinsConfig.getUrl(), jobUri, buildNumber);
        try (var staticHttpSender = mockStatic(HttpSender.class)) {
            staticHttpSender.when(HttpSender::newHttpSender).thenReturn(httpSenderBuilder);
            when(httpSenderBuilder.build()).thenReturn(httpSender);

            jenkinsAdapter.updateBuild(jobUri, buildNumber);

            verify(httpSenderBuilder, times(1)).url(expectedUrl);
            verify(httpSenderBuilder, times(1)).auth(jenkinsAdapter.getAuth());
        }
    }

    @Test
    @DisplayName("Test download artifact")
    @SneakyThrows
    void downloadArtifact() {
        assert jenkinsConfig != null;
        var buildData = new BuildData();
        buildData.setUrl("http://joburl");
        var artifactData = new ArtifactData();
        artifactData.setRelativePath("test/artifact.file");
        var expectedUrl = "%s/%s".formatted(buildData.getUrl(), artifactData.getRelativePath());
        try (var staticHttpSender = mockStatic(HttpSender.class)) {
            staticHttpSender.when(HttpSender::newHttpSender).thenReturn(httpSenderBuilder);
            when(httpSenderBuilder.build()).thenReturn(httpSender);

            jenkinsAdapter.downloadArtifact(buildData, artifactData);

            verify(httpSenderBuilder, times(1)).url(expectedUrl);
            verify(httpSenderBuilder, times(1)).auth(jenkinsAdapter.getAuth());
        }
    }
}