package ru.borun.freedomnet.bot;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import ru.borun.freedomnet.common.Config;

import java.io.IOException;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class BotConfig extends Config {

    private static final BotConfig botConfig = getInstance();
    private String token;

    @SneakyThrows(IOException.class)
    public static BotConfig getInstance() {
        if (botConfig != null) {
            return botConfig;
        } else {
            log.info("Getting Bot config...");
            var configMap = readConfigMap("config.yaml", "bot");
            if (configMap.isPresent()) {
                var config = new ObjectMapper().convertValue(configMap.get(), BotConfig.class);
                log.info("Bot config gotten.");
                return config;
            } else {
                log.warn("Bot config is empty.");
                return new BotConfig();
            }
        }
    }
}