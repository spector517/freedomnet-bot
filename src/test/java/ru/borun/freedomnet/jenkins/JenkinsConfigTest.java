package ru.borun.freedomnet.jenkins;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.borun.freedomnet.common.Config;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Test Bot config load")
class JenkinsConfigTest {

    private static final String CONFIG_PATH = "configs/sample_config.yaml";
    private static final String CONFIG_PREFIX = "jenkins";

    @BeforeAll
    static void init() {
        JenkinsConfig.load(CONFIG_PATH);
    }

    @Test
    @DisplayName("Successfully load Jenkins config")
    @SneakyThrows
    void testGetInstanceSuccess() {
        var expectedJenkinsConfig = new ObjectMapper().convertValue(
                Config.readConfigMap(CONFIG_PATH, CONFIG_PREFIX).get(),
                JenkinsConfig.class
        );
        assertEquals(expectedJenkinsConfig, JenkinsConfig.getInstance());
    }
}