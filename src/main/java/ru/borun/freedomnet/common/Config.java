package ru.borun.freedomnet.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class Config {
    public static Optional<Object> readConfigMap(String configFilePath, String prefix) throws IOException {
        var objectMapper = new ObjectMapper(new YAMLFactory());
        var inputStream = new FileInputStream(configFilePath);
        try {
            var fullMap = objectMapper.readValue(inputStream, Map.class);
            if (!fullMap.containsKey(prefix)) {
                return Optional.empty();
            } else {
                log.debug("Config '{}' deserialized into Map.class", configFilePath);
                return Optional.of(fullMap.get(prefix));
            }
        } catch (IOException exception) {
            log.fatal("Config deserialization error.");
            log.fatal(exception);
            throw exception;
        }
    }
}