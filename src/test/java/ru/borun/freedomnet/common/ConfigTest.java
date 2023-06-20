package ru.borun.freedomnet.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Test config load")
class ConfigTest {

    private static final String CONFIG_PATH = "configs/sample_config.yaml";
    private static final String WRONG_CONFIG_PATH = "configs/wrong_config.yaml";
    private static Map all_config;

    @BeforeAll
    @SneakyThrows
    static void initAll() {
        var objectMapper = new ObjectMapper(new YAMLFactory());
        all_config = objectMapper.readValue(new FileInputStream(CONFIG_PATH), Map.class);
    }

    @ParameterizedTest
    @DisplayName("Successful read scenario")
    @ValueSource(strings = {"jenkins", "bot"})
    @SneakyThrows
    void testReadSuccess(String prefix) {
        var expectedConfig = all_config.get(prefix);
        assertEquals(expectedConfig, Config.readConfigMap(CONFIG_PATH, prefix).get());
    }

    @Test
    @DisplayName("Test wrong resource path")
    void testResourceNull() {
        assertThrows(FileNotFoundException.class, () ->
                Config.readConfigMap(WRONG_CONFIG_PATH, "jenkins")
        );
    }

    @Test
    @DisplayName("Test wrong prefix")
    @SneakyThrows
    void testWrongPrefix() {
        assertEquals(Optional.empty(), Config.readConfigMap(CONFIG_PATH, "wrong"));
    }
}