package ru.borun.freedomnet.jenkins;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import ru.borun.freedomnet.bot.data.ClientData;
import ru.borun.freedomnet.jenkins.data.BuildData;
import ru.borun.freedomnet.jenkins.data.JobData;
import ru.borun.freedomnet.util.http.InvalidHttpStatusCode;

import java.io.IOException;
import java.util.Map;

@Log4j2
@Getter
public class Build {

    private final ClientData clientData;
    private final JenkinsConfig.Job job;
    private final Map<String, String> params;
    private final JenkinsConfig jenkinsConfig;
    private final IJenkinsAdapter jenkinsAdapter;
    private int processingTtl;
    private JobData jobData;
    private BuildData buildData;
    private boolean started;
    private boolean expired;

    public Build(ClientData clientData, JenkinsConfig.Job job, Map<String, String> params,
                 IJenkinsAdapter jenkinsAdapter, JenkinsConfig jenkinsConfig) {
        this.clientData = clientData;
        this.job = job;
        this.params = params;
        this.jenkinsAdapter = jenkinsAdapter;
        this.jenkinsConfig = jenkinsConfig;
        this.processingTtl = jenkinsConfig.getBuildProcessingTtl();
    }

    public void start() throws InterruptedException, InvalidHttpStatusCode, IOException {
        this.jobData = jenkinsAdapter.getJobData(job.getUri());
        this.jenkinsAdapter.runBuild(this.job.getUri(), this.job.getToken(), this.params);
        this.started = true;
        this.updateExpired();
    }

    public void update() throws InvalidHttpStatusCode, IOException, InterruptedException {
        if (this.buildData != null) {
            this.buildData = this.jenkinsAdapter.updateBuild(buildData);
        } else {
            try {
                this.buildData = this.jenkinsAdapter.updateBuild(this.job.getUri(), this.jobData.getNextBuildNumber());
            } catch (InvalidHttpStatusCode ex) {
                log.debug(
                        "Build {}/{}/{} not found",
                        this.jenkinsConfig.getUrl(),
                        this.job.getUri(),
                        this.jobData.getNextBuildNumber()
                );
            }
        }
        this.updateExpired();
    }

    private void updateExpired() {
        this.processingTtl--;
        if (this.processingTtl <= 0) {
            this.expired = true;
        }
    }
}