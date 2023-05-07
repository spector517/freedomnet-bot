package ru.borun.freedomnet.jenkins;

import ru.borun.freedomnet.jenkins.data.ArtifactData;
import ru.borun.freedomnet.jenkins.data.BuildData;
import ru.borun.freedomnet.jenkins.data.JobData;
import ru.borun.freedomnet.util.http.InvalidHttpStatusCode;

import java.io.IOException;
import java.util.Map;

public interface IJenkinsAdapter {
    JobData getJobData(String jobUri)
            throws IOException, InterruptedException, InvalidHttpStatusCode;

    void runBuild(String jobUri, String jobToken, Map<String, String> params)
            throws IOException, InterruptedException, InvalidHttpStatusCode;

    BuildData updateBuild(BuildData buildData)
            throws IOException, InterruptedException, InvalidHttpStatusCode;

    BuildData updateBuild(String jobUri, int buildNumber)
            throws IOException, InterruptedException, InvalidHttpStatusCode;

    byte[] downloadArtifact(BuildData buildData, ArtifactData artifactData)
            throws IOException, InterruptedException, InvalidHttpStatusCode;
}
