package ru.borun.freedomnet.common;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ConfigLoadException extends Exception {
    public ConfigLoadException(String message) {
        super(message);
    }
}
