package ru.borun.freedomnet.jenkins;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import ru.borun.freedomnet.bot.data.ClientData;
import ru.borun.freedomnet.http.HttpSender;
import ru.borun.freedomnet.http.InvalidHttpStatusCode;
import ru.borun.freedomnet.jenkins.data.BuildData;
import ru.borun.freedomnet.jenkins.data.JobData;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Getter
public class Build {

    private static final String AUTH;
    private static final int UPDATE_BUILD_SUCCESS_HTTP_CODE = 200;
    private static final int RUN_BUILD_HTTP_SUCCESS_CODE = 204;
    private static final int GET_JOB_HTTP_SUCCESS_CODE = 200;

    static {
        AUTH = "Basic " + Base64.getEncoder().encodeToString(
                String.format(
                        "%s:%s",
                        JenkinsConfig.getInstance().getUsername(),
                        JenkinsConfig.getInstance().getToken()
                ).getBytes(StandardCharsets.UTF_8)
        );
    }
    private final ClientData clientData;
    private final JenkinsConfig.Job job;
    private final Map<String, String> params;
    private final JenkinsConfig jenkinsConfig;

    private JobData jobData;
    private BuildData buildData;

    public Build(ClientData clientData, JenkinsConfig.Job job, Map<String, String> params)
            throws IOException, InterruptedException, InvalidHttpStatusCode {
        log.info("Run Jenkins job {} for client id {}", job.getUri(), clientData.getClientId());
        this.clientData = clientData;
        this.job = job;
        this.params = params;
        jenkinsConfig = JenkinsConfig.getInstance();
        getJobInfo();
        runBuild();
        var attemptsCount = Math.floorDiv(
                jenkinsConfig.getWaitingBuildMaxTimeout(),
                jenkinsConfig.getPollingInterval()
        );
        for (int i = 1; i <= attemptsCount; i++) {
            try {
                log.debug("Try update build, attempt {}", i);
                update();
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

    private void getJobInfo() throws IOException, InterruptedException, InvalidHttpStatusCode {
        log.debug("Get job data from Jenkins...");
        var url = String.format("%s/%s/api/json", jenkinsConfig.getUrl(), job.getUri());
        jobData = HttpSender.newHttpSender()
                .url(url)
                .auth(AUTH)
                .build()
                .sendRequest(JobData.class, List.of(GET_JOB_HTTP_SUCCESS_CODE));
        log.debug("Job data received.");
    }

    private void runBuild() throws IOException, InterruptedException, InvalidHttpStatusCode {
        var pars = new LinkedHashMap<>(params);
        pars.put("token", job.getToken());
        log.debug(
                """
                        Run job {} with parameters:
                        {}""",
                jobData.getUrl(), pars
        );
        HttpSender.newHttpSender()
                .url(String.format("%sbuildWithParameters", jobData.getUrl()))
                .auth(AUTH)
                .queryParams(pars)
                .build()
                .sendRequest(List.of(RUN_BUILD_HTTP_SUCCESS_CODE));
        log.debug("Build started.");
    }

    public void update() throws IOException, InterruptedException, InvalidHttpStatusCode {
        log.debug("Updating build...");
        var url = String.format("%s%d/api/json", jobData.getUrl(), jobData.getNextBuildNumber());
        buildData = HttpSender.newHttpSender()
                .url(url)
                .auth(AUTH)
                .build()
                .sendRequest(BuildData.class, List.of(UPDATE_BUILD_SUCCESS_HTTP_CODE));
        log.debug("Build updated.");
    }
}