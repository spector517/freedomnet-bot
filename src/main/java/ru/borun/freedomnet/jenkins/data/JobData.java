package ru.borun.freedomnet.jenkins.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JobData {
    private String url;
    private int nextBuildNumber;
}