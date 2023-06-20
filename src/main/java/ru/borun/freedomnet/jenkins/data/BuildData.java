package ru.borun.freedomnet.jenkins.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildData {

    private String url;
    private String displayName;
    private int number;
    private BuildResults result;
    private boolean inProgress;
    private List<ArtifactData> artifactData;
}
