package ru.borun.freedomnet.jenkins;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Log4j2
public class BuildService implements Runnable {

    private static final BuildService buildService = getInstance();
    public static BuildService getInstance() {
        return Objects.requireNonNullElseGet(buildService, BuildService::new);
    }
    private final Queue<Build> processingBuilds;
    private final Queue<Build> finishedBuilds;
    private final JenkinsConfig jenkinsConfig;
    private boolean stopped;

    private BuildService() {
        processingBuilds = new ConcurrentLinkedQueue<>();
        finishedBuilds = new ConcurrentLinkedQueue<>();
        jenkinsConfig = JenkinsConfig.getInstance();
    }

    public void addBuildToProcessing(Build build) {
        log.info("Add build for client ({}) to processing", build.getClientData().getClientId());
        processingBuilds.add(build);
    }

    public Build getFinishedBuild() {
        return finishedBuilds.poll();
    }

    public void stop() {
        log.info("Stopping build service...");
        stopped = true;
    }

    @SneakyThrows
    @Override
    public void run() {
        while (!stopped) {
            log.debug("Sleeping for {} sec", jenkinsConfig.getPollingInterval() / 1000);
            Thread.sleep(jenkinsConfig.getPollingInterval());
            var build = processingBuilds.poll();
            if (build == null) {
                continue;
            }
            build.update();
            if (build.getBuildData().getResult() == null) {
                processingBuilds.add(build);
            } else {
                finishedBuilds.add(build);
                log.info("Build for client ({}) processed.", build.getClientData().getClientId());
            }
        }
        log.info("Build service stopped.");
    }
}