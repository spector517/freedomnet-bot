package ru.borun.freedomnet.bot;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.extern.log4j.Log4j2;
import ru.borun.freedomnet.common.Config;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode(callSuper = false)
public class BotConfig extends Config {

    private static final BotConfig INSTANCE = getInstance();
    private static final String CONFIG_RESOURCE_PATH = "config.yaml";
    private static final String CONFIG_PREFIX = "bot";
    private String token;

    @SneakyThrows
    public static BotConfig getInstance() {
        if (INSTANCE != null) {
            return INSTANCE;
        } else {
            log.info("Getting Bot config...");
            var configMap = readConfigMap(CONFIG_RESOURCE_PATH, CONFIG_PREFIX);
            if (configMap.isPresent()) {
                var config = new ObjectMapper().convertValue(configMap.get(), BotConfig.class);
                log.info("Bot config gotten.");
                return config;
            } else {
                var message = "Bot config is empty.";
                log.fatal(message);
                return null;
            }
        }
    }
}