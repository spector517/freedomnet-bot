package ru.borun.freedomnet.jenkins;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import ru.borun.freedomnet.jenkins.data.BuildData;
import ru.borun.freedomnet.jenkins.data.JobData;
import ru.borun.freedomnet.util.http.InvalidHttpStatusCode;

import java.io.IOException;
import java.util.Map;

@Log4j2
@Getter
public class Build {

    private final JenkinsConfig.Job job;
    private final Map<String, String> params;
    private final JenkinsConfig jenkinsConfig;
    private final IJenkinsAdapter jenkinsAdapter;
    private int processingTtl;
    private JobData jobData;
    private BuildData buildData;
    private long clientId;
    private boolean started;
    private boolean expired;

    public Build(JenkinsConfig.Job job, Map<String, String> params, IJenkinsAdapter jenkinsAdapter,
                 JenkinsConfig jenkinsConfig, long clientId) {
        this.job = job;
        this.params = params;
        this.jenkinsAdapter = jenkinsAdapter;
        this.jenkinsConfig = jenkinsConfig;
        this.clientId = clientId;
        processingTtl = jenkinsConfig.getBuildProcessingTtl();
    }

    public void start() throws InterruptedException, InvalidHttpStatusCode, IOException {
        jobData = jenkinsAdapter.getJobData(job.getUri());
        jenkinsAdapter.runBuild(job.getUri(), job.getToken(), params);
        started = true;
        updateExpired();
    }

    public void update() throws InvalidHttpStatusCode, IOException, InterruptedException {
        if (buildData != null) {
            buildData = jenkinsAdapter.updateBuild(buildData);
        } else {
            try {
                buildData = jenkinsAdapter.updateBuild(job.getUri(), jobData.getNextBuildNumber());
            } catch (InvalidHttpStatusCode ex) {
                log.debug(
                        "Build {}/{}/{} not found",
                        jenkinsConfig.getUrl(),
                        job.getUri(),
                        jobData.getNextBuildNumber()
                );
            }
        }
        updateExpired();
    }

    private void updateExpired() {
        processingTtl--;
        if (processingTtl <= 0) {
            expired = true;
        }
    }
}