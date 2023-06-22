package ru.borun.freedomnet;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import ru.borun.freedomnet.bot.BotConfig;
import ru.borun.freedomnet.common.SSLConfig;
import ru.borun.freedomnet.jenkins.BuildService;
import ru.borun.freedomnet.jenkins.JenkinsConfig;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.SecureRandom;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Main {

    @SneakyThrows
    public static void main(String[] args) {
        JenkinsConfig.load(args[0]);
        SSLConfig.load(args[0]);
        BotConfig.load(args[0]);
        applySslConfig(SSLConfig.getInstance());
        startBuildService(JenkinsConfig.getInstance());
    }

    private static BuildService startBuildService(JenkinsConfig jenkinsConfig) {
        var buildService = new BuildService(jenkinsConfig);
        var buildProcessingThread = new Thread(buildService);
        buildProcessingThread.setName("Build processing thread");
        buildProcessingThread.start();
        log.info("Build service started");
        return buildService;
    }

    @SneakyThrows
    private static void applySslConfig(SSLConfig sslConfig) {
        if (!sslConfig.isSslEnabled()) {
            log.warn("SSL verify is disabled.");
            return;
        }
        try {
            var keystore = KeyStore.getInstance("JKS");
            try(var fileInputStream = new FileInputStream(sslConfig.getKeystorePath())) {
                keystore.load(fileInputStream, sslConfig.getKeystorePassword().toCharArray());
            }

            var trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm()
            );
            var keyManagerFactory = KeyManagerFactory.getInstance(
                    KeyManagerFactory.getDefaultAlgorithm()
            );
            trustManagerFactory.init(keystore);
            keyManagerFactory.init(keystore, sslConfig.getKeyPassword().toCharArray());

            var sslContext = SSLContext.getInstance("TLS");
            sslContext.init(
                    keyManagerFactory.getKeyManagers(),
                    trustManagerFactory.getTrustManagers(),
                    new SecureRandom()
            );
            SSLContext.setDefault(sslContext);
            log.info("SSL context updated");
        } catch (Exception ex) {
            log.fatal("Error loading JKS\n{}", ex.getMessage());
            throw ex;
        }
    }
}