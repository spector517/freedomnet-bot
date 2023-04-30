package ru.borun.freedomnet.jenkins;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import ru.borun.freedomnet.jenkins.data.BuildData;
import ru.borun.freedomnet.jenkins.data.JobData;
import ru.borun.freedomnet.util.http.HttpSender;
import ru.borun.freedomnet.util.http.InvalidHttpStatusCode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Getter
public class JenkinsAdapter {

    public static final int UPDATE_BUILD_SUCCESS_HTTP_CODE = 200;
    public static final int RUN_BUILD_HTTP_SUCCESS_CODE = 204;
    public static final int GET_JOB_HTTP_SUCCESS_CODE = 200;

    private final JenkinsConfig jenkinsConfig;
    private final String auth;

    public JenkinsAdapter(JenkinsConfig jenkinsConfig) {
        this.jenkinsConfig = jenkinsConfig;
        this.auth = "Basic + " + Base64.getEncoder().encodeToString(
                "%s:%s".formatted(jenkinsConfig.getUsername(), jenkinsConfig.getToken()
                ).getBytes(StandardCharsets.UTF_8)
        );
    }

    public JobData getJobData(String jobUri)
            throws IOException, InterruptedException, InvalidHttpStatusCode {
        log.debug("Get job data from Jenkins...");
        var url = String.format("%s/%s/api/json", jenkinsConfig.getUrl(), jobUri);
        var jobData = HttpSender.newHttpSender()
                .url(url)
                .auth(auth)
                .build()
                .sendRequest(JobData.class, List.of(GET_JOB_HTTP_SUCCESS_CODE));
        log.debug("Job data received.");
        return jobData;
    }

    public void runBuild(String jobUri, String jobToken, Map<String, String> params)
            throws IOException, InterruptedException, InvalidHttpStatusCode {
        var pars = new LinkedHashMap<>(params);
        pars.put("token", jobToken);
        log.debug(
                """
                        Run job {} with parameters:
                        {}""",
                jobUri, pars
        );
        HttpSender.newHttpSender()
                .url(String.format("%s/%s/buildWithParameters", jenkinsConfig.getUrl(), jobUri))
                .auth(auth)
                .queryParams(pars)
                .build()
                .sendRequest(List.of(RUN_BUILD_HTTP_SUCCESS_CODE));
        log.debug("Build started.");
    }

    public BuildData updateBuild(BuildData buildData)
            throws IOException, InterruptedException, InvalidHttpStatusCode {
        log.debug("Updating build %s ...".formatted(buildData.getUrl()));
        var url = String.format("%s/api/json", buildData.getUrl());
        var updatedBuildData = HttpSender.newHttpSender()
                .url(url)
                .auth(auth)
                .build()
                .sendRequest(BuildData.class, List.of(UPDATE_BUILD_SUCCESS_HTTP_CODE));
        log.debug("Build updated.");
        return updatedBuildData;
    }

    public BuildData updateBuild(String jobUri, int buildNumber)
            throws InvalidHttpStatusCode, IOException, InterruptedException {
        log.debug("Updating build {}/{}/{} ...", jenkinsConfig.getUrl(), jobUri, buildNumber);
        var url = String.format("%s/%s/%d/api/json", jenkinsConfig.getUrl(), jobUri, buildNumber);
        var updatedBuildData = HttpSender.newHttpSender()
                .url(url)
                .auth(auth)
                .build()
                .sendRequest(BuildData.class, List.of(UPDATE_BUILD_SUCCESS_HTTP_CODE));
        log.debug("Build updated.");
        return updatedBuildData;
    }
}