package ru.borun.freedomnet.jenkins;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.borun.freedomnet.common.Config;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class JenkinsConfig extends Config {
    private static final JenkinsConfig INSTANCE = getInstance();
    private String url;
    private String username;
    private String token;
    @JsonProperty(value = "proxy_deploy_job")
    private Job proxyDeployJob;
    @JsonProperty(value = "openvpn_deploy_job")
    private Job openvpnDeployJob;
    @JsonProperty(value = "polling_interval")
    private long pollingInterval;

    @JsonProperty(value = "waiting_build_max_timeout")
    private long waitingBuildMaxTimeout;

    @Getter
    public static class Job {
        private String uri;
        private String token;
    }

    public static JenkinsConfig getInstance() {
        if (INSTANCE != null) {
            return INSTANCE;
        } else {
            var configMap = readConfigMap("config.yaml", "jenkins");
            if (configMap.isPresent()) {
                return new ObjectMapper().convertValue(configMap.get(), JenkinsConfig.class);
            } else {
                return new JenkinsConfig();
            }
        }
    }
}