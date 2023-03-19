package ru.borun.freedomnet.jenkins;

import lombok.Getter;
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

@Getter
public class Build {

    private static final String AUTH;

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
        this.clientData = clientData;
        this.job = job;
        this.params = params;
        jenkinsConfig = JenkinsConfig.getInstance();
        setJobData();
        runBuild();
        var attemptsCount = Math.floorDiv(
                jenkinsConfig.getWaitingBuildMaxTimeout(),
                jenkinsConfig.getPollingInterval()
        );
        for (int i = 0; i <= attemptsCount; i++) {
            try {
                update();
            } catch (InvalidHttpStatusCode ex) {
                if (i == attemptsCount) {
                    throw ex;
                }
                Thread.sleep(jenkinsConfig.getPollingInterval());
            }
        }
    }
    private void setJobData() throws IOException, InterruptedException, InvalidHttpStatusCode {
        var url = String.format("%s/%s/api/json", jenkinsConfig.getUrl(), job.getUri());
        jobData = HttpSender.newHttpSender()
                .url(url)
                .auth(AUTH)
                .build()
                .sendRequest(JobData.class);
    }

    private void runBuild() throws IOException, InterruptedException, InvalidHttpStatusCode {
        var pars = new LinkedHashMap<>(params);
        pars.put("token", job.getToken());
        HttpSender.newHttpSender()
                .url(String.format("%sbuildWithParameters", jobData.getUrl()))
                .auth(AUTH)
                .queryParams(pars)
                .build()
                .sendRequest();
    }

    public void update() throws IOException, InterruptedException, InvalidHttpStatusCode {
        var url = String.format("%s%d/api/json", jobData.getUrl(), jobData.getNextBuildNumber());
        buildData = HttpSender.newHttpSender()
                .url(url)
                .auth(AUTH)
                .build()
                .sendRequest(BuildData.class, List.of(200));
    }
}
