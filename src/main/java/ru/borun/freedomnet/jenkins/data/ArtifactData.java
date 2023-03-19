package ru.borun.freedomnet.jenkins.data;

import lombok.Data;

@Data
public class ArtifactData {

    private String displayPath;
    private String fileName;
    private String relativePath;
}
