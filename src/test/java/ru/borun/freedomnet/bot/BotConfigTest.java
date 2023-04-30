package ru.borun.freedomnet.bot;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import ru.borun.freedomnet.common.Config;

import java.util.Optional;

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
        Assertions.assertEquals(expectedBotConfig, BotConfig.getInstance());
    }
}