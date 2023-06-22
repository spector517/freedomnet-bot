package ru.borun.freedomnet.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.extern.log4j.Log4j2;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode(callSuper = false)
public class SSLConfig extends Config {
    private static final String CONFIG_PREFIX = "ssl";
    private static SSLConfig instance;
    @JsonProperty(value = "ssl_enabled") private boolean sslEnabled;
    @JsonProperty(value = "keystore_path") private String keystorePath;
    @JsonProperty(value = "keystore_password") private String keystorePassword;
    @JsonProperty(value = "key_password") private String keyPassword;

    @SneakyThrows
    public static void load(String configFilePath) {
        log.info("Loading SSL config...");
        var configMap = readConfigMap(configFilePath, CONFIG_PREFIX);
        if (configMap.isPresent()) {
            var config = new ObjectMapper().convertValue(configMap.get(), SSLConfig.class);
            log.info("SSL config loaded.");
            instance = config;
        } else {
            var message = "SSL config is empty.";
            log.fatal(message);
            throw new ConfigLoadException(message);
        }
    }

    @SneakyThrows
    public static SSLConfig getInstance() {
        if (instance == null) {
            var message = "SSL config is empty.";
            log.fatal(message);
            throw new ConfigLoadException(message);
        }
        return instance;
    }
}
