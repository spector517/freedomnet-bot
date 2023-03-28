package ru.borun.freedomnet.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class Config {
    public static Optional<Object> readConfigMap(String resourcePath, String prefix) throws IOException {
        var objectMapper = new ObjectMapper(new YAMLFactory());
        var inputStream = Config.class.getClassLoader().getResourceAsStream(resourcePath);
        try {
            if (inputStream == null) {
                throw new IOException(String.format("The input stream of resource '%s' is null", resourcePath));
            } else {
                var fullMap = objectMapper.readValue(inputStream, Map.class);
                if (!fullMap.containsKey(prefix)) {
                    return Optional.empty();
                } else {
                    log.debug("Config '{}' deserialized into Map.class", resourcePath);
                    return Optional.of(fullMap.get(prefix));
                }
            }
        } catch (IOException exception) {
            log.fatal("Config deserialization error.");
            log.fatal(exception);
            throw exception;
        }
    }
}