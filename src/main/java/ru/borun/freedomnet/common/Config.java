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
    protected static Optional<Object> readConfigMap(String resourcePath, String prefix) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        var inputStream = Config.class.getClassLoader().getResourceAsStream(resourcePath);
        try {
            if (inputStream == null) {
                throw new IOException(String.format("The input stream of resource '%s' is null", resourcePath));
            } else {
                var result = Optional.of(objectMapper.readValue(inputStream, Map.class).get(prefix));
                inputStream.close();
                log.debug("Config '{}' deserialized into Map.class", resourcePath);
                return result;
            }
        } catch (IOException exception) {
            log.fatal("Config deserialization error.");
            log.fatal(exception.getMessage());
            throw exception;
        }
    }
}