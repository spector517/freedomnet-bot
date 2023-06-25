package ru.borun.freedomnet.jenkins;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.borun.freedomnet.bot.data.ClientData;
import ru.borun.freedomnet.bot.data.ClientLang;
import ru.borun.freedomnet.jenkins.data.BuildData;
import ru.borun.freedomnet.jenkins.data.JobData;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@DisplayName("Test build create/update/expire")
class BuildTest {

    private static final String CONFIG_PATH = "configs/sample_config.yaml";

    private JenkinsConfig jenkinsConfig;
    private IJenkinsAdapter jenkinsAdapter;
    private JenkinsConfig.Job job;
    private Map<String, String> params;
    private JobData jobData;
    private BuildData buildData;

    @BeforeAll
    static void init() {
        JenkinsConfig.load(CONFIG_PATH);
    }

    @BeforeEach
    @SneakyThrows
    void setUp() {
        jenkinsConfig = JenkinsConfig.getInstance();
        jenkinsAdapter = mock(JenkinsAdapter.class);
        assert jenkinsConfig != null;
        job = jenkinsConfig.getWireguardDeployJob();
        params = Map.of("k1", "v1");
        jobData = JobData.builder()
                .url("https://someurl")
                .nextBuildNumber(100)
                .build();
        buildData = BuildData.builder()
                .url("https//someurl/100")
                .build();
    }

    @Test
    @DisplayName("Test start build")
    @SneakyThrows
    void start() {
        when(jenkinsAdapter.getJobData(job.getUri())).thenReturn(jobData);
        var build = new Build(job, params, jenkinsAdapter, jenkinsConfig, 0);
        build.start();
        verify(jenkinsAdapter, times(1)).getJobData(job.getUri());
        verify(jenkinsAdapter, times(1))
                .runBuild(job.getUri(), job.getToken(), params);
        assertTrue(build.isStarted());
    }

    @Test
    @DisplayName("Test update build by build number")
    @SneakyThrows
    void update() {
        when(jenkinsAdapter.getJobData(job.getUri())).thenReturn(jobData);
        when(jenkinsAdapter.updateBuild(job.getUri(), jobData.getNextBuildNumber())).thenReturn(buildData);
        var build = new Build(job, params, jenkinsAdapter, jenkinsConfig, 0);
        build.start();
        build.update();
        verify(jenkinsAdapter, times(1)).updateBuild(job.getUri(), jobData.getNextBuildNumber());
        build.update();
        verify(jenkinsAdapter, times(1)).updateBuild(buildData);
    }

    @Test
    @DisplayName("Test expiration of build")
    @SneakyThrows
    void expire() {
        when(jenkinsAdapter.getJobData(job.getUri())).thenReturn(jobData);
        when(jenkinsAdapter.updateBuild(job.getUri(), jobData.getNextBuildNumber()))
                .thenReturn(buildData);
        var build = new Build(job, params, jenkinsAdapter, jenkinsConfig, 0);
        build.start();
        int i = 0;
        while (i < jenkinsConfig.getBuildProcessingTtl() - 1) {
            build.update();
            i++;
        }
        assertTrue(build.isExpired());
    }
}