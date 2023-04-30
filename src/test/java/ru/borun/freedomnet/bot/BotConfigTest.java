package ru.borun.freedomnet.bot;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.borun.freedomnet.common.Config;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Test Bot config load")
class BotConfigTest {

    private static final String CONFIG_PATH = "config.yaml";
    private static final String CONFIG_PREFIX = "bot";

    @Test
    @DisplayName("Successfully load Bot config")
    @SneakyThrows
    void testGetInstanceSuccess() {
        var expectedBotConfig = new ObjectMapper().convertValue(
                Config.readConfigMap(CONFIG_PATH, CONFIG_PREFIX).get(),
                BotConfig.class
        );
        assertEquals(expectedBotConfig, BotConfig.getInstance());
    }
}