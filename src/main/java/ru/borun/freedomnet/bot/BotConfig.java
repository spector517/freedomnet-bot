package ru.borun.freedomnet.bot;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.extern.log4j.Log4j2;
import ru.borun.freedomnet.common.Config;
import ru.borun.freedomnet.common.ConfigLoadException;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode(callSuper = false)
public class BotConfig extends Config {

    private static final String CONFIG_PREFIX = "bot";
    private static BotConfig instance;
    private String name;
    private String token;

    @SneakyThrows
    public static void load(String configFilePath) {
        log.info("Loading Bot config...");
        var configMap = readConfigMap(configFilePath, CONFIG_PREFIX);
        if (configMap.isPresent()) {
            instance = new ObjectMapper().convertValue(configMap.get(), BotConfig.class);
            log.info("Bot config loaded.");
        } else {
            var message = "Bot config is empty.";
            log.fatal(message);
            throw new ConfigLoadException(message);
        }
    }

    @SneakyThrows
    public static BotConfig getInstance() {
        if (instance == null) {
            var message = "Bot config is empty.";
            log.fatal(message);
            throw new ConfigLoadException(message);
        }
        return instance;
    }
}