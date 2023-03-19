package ru.borun.freedomnet.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class Config {
    protected static Optional<Object> readConfigMap(String resourcePath, String prefix) {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        var inputStream = Config.class.getClassLoader().getResourceAsStream(resourcePath);
        try {
            return Optional.of(objectMapper.readValue(inputStream, Map.class).get(prefix));
        } catch (IOException exception) {
            return Optional.empty();
        }
    }
}