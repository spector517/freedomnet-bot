package ru.borun.freedomnet.jenkins;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.extern.log4j.Log4j2;
import ru.borun.freedomnet.common.Config;
import ru.borun.freedomnet.common.ConfigLoadException;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode(callSuper = false)
public class JenkinsConfig extends Config {
    private static final String CONFIG_PREFIX = "jenkins";
    private static JenkinsConfig instance;
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

    @SneakyThrows
    public static void load(String configFilePath) {
        log.info("Loading Jenkins config...");
        var configMap = readConfigMap(configFilePath, CONFIG_PREFIX);
        if (configMap.isPresent()) {
            instance = new ObjectMapper().convertValue(configMap.get(), JenkinsConfig.class);
            log.info("Jenkins config loaded.");
        } else {
            var message = "Bot config is empty.";
            log.fatal(message);
            throw new ConfigLoadException(message);
        }
    }

    @SneakyThrows
    public static JenkinsConfig getInstance() {
        if (instance == null) {
            var message = "Jenkins config is empty.";
            log.fatal(message);
            throw new ConfigLoadException(message);
        }
        return instance;
    }
}