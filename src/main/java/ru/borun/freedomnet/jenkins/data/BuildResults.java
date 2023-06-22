package ru.borun.freedomnet.jenkins.data;

import com.fasterxml.jackson.annotation.JsonValue;

public enum BuildResults {
    SUCCESS("SUCCESS"),
    UNSTABLE("UNSTABLE"),
    FAILURE("FAILURE"),
    ABORTED("ABORTED");

    private final String result;

    BuildResults(String result) {
        this.result = result;
    }

    @JsonValue
    private String getResult() {
        return result;
    }

}
