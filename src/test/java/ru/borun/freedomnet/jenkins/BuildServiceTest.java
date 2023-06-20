package ru.borun.freedomnet.jenkins;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import ru.borun.freedomnet.bot.data.ClientData;
import ru.borun.freedomnet.bot.data.ClientLang;
import ru.borun.freedomnet.jenkins.data.BuildData;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeout;
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
        buildService = new BuildService(jenkinsConfig);
        build = mock(Build.class);
        when(build.getClientData()).thenReturn(
                ClientData.builder()
                        .lang(ClientLang.RU)
                        .clientId(100500)
                        .build()
        );
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
    @DisplayName("Test start build processing")
    @SneakyThrows
    void startBuildProcessing() {
        when(build.isStarted()).thenReturn(false);
        buildService.addBuildToProcessing(build);
        verify(build, timeout(jenkinsConfig.getPollingInterval() * 2).times(1)).start();
    }

    @Test
    @DisplayName("Test update build processing")
    @SneakyThrows
    void updateBuildProcessing() {
        buildService.addBuildToProcessing(build);
        when(build.isStarted()).thenReturn(true);
        var buildData = BuildData.builder()
                .inProgress(true)
                .build();
        when(build.getBuildData()).thenReturn(buildData);
        verify(build, timeout(jenkinsConfig.getPollingInterval() * 2).times(1)).update();
    }

    @Test
    @DisplayName("Test finish build processing")
    @SneakyThrows
    void finishBuildProcessing() {
        buildService.addBuildToProcessing(build);
        when(build.isStarted()).thenReturn(true);
        var buildData = BuildData.builder()
                .inProgress(false)
                .build();
        when(build.getBuildData()).thenReturn(buildData);
        verify(build, timeout(jenkinsConfig.getPollingInterval() * 2).times(1)).update();
        Thread.sleep(jenkinsConfig.getPollingInterval() * 2);
        assertEquals(build, buildService.getFinishedBuild());
    }
}