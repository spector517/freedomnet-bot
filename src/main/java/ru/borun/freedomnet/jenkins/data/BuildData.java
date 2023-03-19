package ru.borun.freedomnet.jenkins.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import ru.borun.freedomnet.jenkins.BuildResult;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildData {

    private String url;
    private String displayName;
    private int number;
    private BuildResult result;
    private boolean inProgress;
    private List<ArtifactData> artifactData;
}
