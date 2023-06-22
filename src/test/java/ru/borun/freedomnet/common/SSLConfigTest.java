package ru.borun.freedomnet.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Test SSL config load")
class SSLConfigTest {

    private static final String CONFIG_PATH = "configs/sample_config.yaml";
    private static final String CONFIG_PREFIX = "ssl";

    @BeforeAll
    static void init() {
        SSLConfig.load(CONFIG_PATH);
    }

    @Test
    @DisplayName("Successfully load SSL config")
    @SneakyThrows
    void testGetInstanceSuccess() {
        var expectedSslConfig = new ObjectMapper().convertValue(
                Config.readConfigMap(CONFIG_PATH, CONFIG_PREFIX).get(),
                SSLConfig.class
        );
        assertEquals(expectedSslConfig, SSLConfig.getInstance());
    }
}