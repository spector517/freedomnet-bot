package ru.borun.freedomnet.bot;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.borun.freedomnet.common.Config;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class BotConfig extends Config {

    private static final BotConfig botConfig = getInstance();
    private String token;

    public static BotConfig getInstance() {
        if (botConfig != null) {
            return botConfig;
        } else {
            var configMap = readConfigMap("config.yaml", "bot");
            if (configMap.isPresent()) {
                return new ObjectMapper().convertValue(configMap.get(), BotConfig.class);
            } else {
                return new BotConfig();
            }
        }
    }
}