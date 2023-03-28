package ru.borun.freedomnet.jenkins;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import ru.borun.freedomnet.common.Config;

import java.util.Optional;

@DisplayName("Test Bot config load")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JenkinsConfigTest {

    private static final String CONFIG_PATH = "config.yaml";
    private static final String CONFIG_PREFIX = "jenkins";

    @Test
    @Order(1)
    @DisplayName("Invalid config.yaml (no 'jenkins' section)")
    @SneakyThrows
    void testInvalidConfig() {
        try (var configMock = Mockito.mockStatic(Config.class)) {
            configMock.when(() -> Config.readConfigMap(CONFIG_PATH, CONFIG_PREFIX))
                    .thenReturn(Optional.empty());
            Assertions.assertNull(JenkinsConfig.getInstance());
        }
    }

    @Test
    @Order(2)
    @DisplayName("Successfully load Jenkins config")
    @SneakyThrows
    void testGetInstanceSuccess() {
        var expectedJenkinsConfig = new ObjectMapper().convertValue(
                Config.readConfigMap(CONFIG_PATH, CONFIG_PREFIX).get(),
                JenkinsConfig.class
        );
        Assertions.assertEquals(expectedJenkinsConfig, JenkinsConfig.getInstance());
    }
}