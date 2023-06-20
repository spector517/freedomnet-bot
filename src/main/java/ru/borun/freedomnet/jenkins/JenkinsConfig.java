package ru.borun.freedomnet.jenkins;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.extern.log4j.Log4j2;
import ru.borun.freedomnet.common.Config;

import java.io.IOException;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode(callSuper = false)
public class JenkinsConfig extends Config {
    private static final JenkinsConfig INSTANCE = getInstance();
    private static final String CONFIG_RESOURCE_PATH = "config.yaml";
    private static final String CONFIG_PREFIX = "jenkins";
    private String url;
    private String username;
    private String token;
    @JsonProperty(value = "proxy_deploy_job") private Job proxyDeployJob;
    @JsonProperty(value = "openvpn_deploy_job") private Job openvpnDeployJob;
    @JsonProperty(value = "wireguard_deploy_job") private Job wireguardDeployJob;

    @JsonProperty(value = "polling_interval") private long pollingInterval;
    @JsonProperty(value = "build_processing_ttl") private int buildProcessingTtl;

    @Getter
    @EqualsAndHashCode
    public static class Job {
        private String uri;
        private String token;
    }

    @SneakyThrows(IOException.class)
    public static JenkinsConfig getInstance() {
        if (INSTANCE != null) {
            return INSTANCE;
        } else {
            log.info("Getting Jenkins config...");
            var configMap = readConfigMap(CONFIG_RESOURCE_PATH, CONFIG_PREFIX);
            if (configMap.isPresent()) {
                var config = new ObjectMapper().convertValue(configMap.get(), JenkinsConfig.class);
                log.info("Jenkins config gotten.");
                return config;
            } else {
                var message = "Jenkins config is empty.";
                log.fatal(message);
                return null;
            }
        }
    }
}