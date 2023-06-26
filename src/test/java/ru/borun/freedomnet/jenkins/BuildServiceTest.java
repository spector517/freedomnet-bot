package ru.borun.freedomnet.jenkins;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import ru.borun.freedomnet.jenkins.data.BuildData;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

@DisplayName("Test build processing service")
class BuildServiceTest {

    private static final String CONFIG_PATH = "configs/sample_config.yaml";

    private JenkinsConfig jenkinsConfig;
    private BuildService buildService;
    private Thread serviceThread;

    private Build build;

    @BeforeAll
    static void init() {
        JenkinsConfig.load(CONFIG_PATH);
    }

    @BeforeEach
    void setUp() {
        jenkinsConfig = JenkinsConfig.getInstance();
        buildService = BuildService.getInstance();
        build = mock(Build.class);
        serviceThread = new Thread(buildService);
        serviceThread.setName("Test build service thread");
        serviceThread.start();
    }

    @AfterEach
    void tearDown() {
        if (serviceThread.isAlive()) {
            serviceThread.interrupt();
        }
    }

    @Test
    @DisplayName("Complex build processing test")
    @SneakyThrows
    void complexBuildProcessing() {
        when(build.isStarted()).thenReturn(false);
        buildService.addBuildToProcessing(build);
        verify(build, timeout(jenkinsConfig.getPollingInterval() * 2).times(1)).start();

        when(build.isStarted()).thenReturn(true);
        var buildData = BuildData.builder()
                .inProgress(true)
                .build();
        when(build.getBuildData()).thenReturn(buildData);
        verify(build, timeout(jenkinsConfig.getPollingInterval() * 2).times(1)).update();

        buildData = BuildData.builder()
                .inProgress(false)
                .build();
        when(build.getBuildData()).thenReturn(buildData);
        await().atMost(Duration.ofMillis(jenkinsConfig.getPollingInterval() * 2)).until(() ->
                build.equals(buildService.getFinishedBuild())
        );
    }
}