package ru.borun.freedomnet.jenkins.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArtifactData {

    private String displayPath;
    private String fileName;
    private String relativePath;
}
