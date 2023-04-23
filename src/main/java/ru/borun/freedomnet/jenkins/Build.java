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
import java.util.Objects;

@Log4j2
@Getter
public class Build {

    private final ClientData clientData;
    private final JenkinsConfig.Job job;
    private final Map<String, String> params;
    private final JenkinsConfig jenkinsConfig;
    private final JenkinsAdapter jenkinsAdapter;

    private final JobData jobData;
    @Setter
    private BuildData buildData;

    public Build(ClientData clientData, JenkinsConfig.Job job, Map<String, String> params)
            throws IOException, InterruptedException, InvalidHttpStatusCode {
        log.info("Run Jenkins job {} for client id {}", job.getUri(), clientData.getClientId());
        this.clientData = clientData;
        this.job = job;
        this.params = params;
        jenkinsConfig = JenkinsConfig.getInstance();
        jenkinsAdapter = new JenkinsAdapter(Objects.requireNonNull(jenkinsConfig));
        jobData = jenkinsAdapter.getJobData(job.getUri());
        jenkinsAdapter.runBuild(job.getUri(), job.getToken(), params);
        var attemptsCount = Math.floorDiv(
                jenkinsConfig.getWaitingBuildMaxTimeout(),
                jenkinsConfig.getPollingInterval()
        );
        for (int i = 1; i <= attemptsCount; i++) {
            try {
                log.debug("Try update build, attempt {}", i);
                buildData = jenkinsAdapter.updateBuild(job.getUri(), jobData.getNextBuildNumber());
                break;
            } catch (InvalidHttpStatusCode ex) {
                if (i == attemptsCount) {
                    log.error("Max retries ({}) exceeded.", attemptsCount);
                    log.error(ex);
                    throw ex;
                }
                log.debug("Update failed.");
                log.debug("Sleeping for {} sec...", jenkinsConfig.getPollingInterval() / 1000);
                Thread.sleep(jenkinsConfig.getPollingInterval());
            }
        }
        log.info("Build started successfully: {}.", buildData.getUrl());
    }
}