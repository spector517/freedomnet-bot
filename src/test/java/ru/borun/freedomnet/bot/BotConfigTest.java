package ru.borun.freedomnet.bot;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import ru.borun.freedomnet.common.Config;

import java.util.Optional;

@DisplayName("Test Bot config load")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BotConfigTest {

    private static final String CONFIG_PATH = "config.yaml";
    private static final String CONFIG_PREFIX = "bot";

    @Test
    @Order(1)
    @DisplayName("Invalid config.yaml (no 'bot' section)")
    @SneakyThrows
    void testInvalidConfig() {
        try (var configMock = Mockito.mockStatic(Config.class)) {
            configMock.when(() -> Config.readConfigMap(CONFIG_PATH, CONFIG_PREFIX))
                    .thenReturn(Optional.empty());
            Assertions.assertNull(BotConfig.getInstance());
        }
    }

    @Test
    @Order(2)
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