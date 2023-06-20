package ru.borun.freedomnet.jenkins;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Log4j2
public class BuildService implements Runnable {

    private final Queue<Build> processingBuildsQueue;
    private final Queue<Build> finishedBuildsQueue;
    private final JenkinsConfig jenkinsConfig;

    public BuildService(JenkinsConfig jenkinsConfig) {
        processingBuildsQueue = new ConcurrentLinkedQueue<>();
        finishedBuildsQueue = new ConcurrentLinkedQueue<>();
        this.jenkinsConfig = jenkinsConfig;
    }

    public void addBuildToProcessing(Build build) {
        log.info("Add build for client ({}) to processing", build.getClientData().getClientId());
        processingBuildsQueue.add(build);
    }

    public Build getFinishedBuild() {
        return finishedBuildsQueue.poll();
    }

    @SneakyThrows
    @Override
    public void run() {
        while (!Thread.interrupted()) {
            log.debug("Sleeping for {} sec", jenkinsConfig.getPollingInterval() / 1000);
            Thread.sleep(jenkinsConfig.getPollingInterval());
            var build = processingBuildsQueue.poll();
            if (build == null) {
                continue;
            }
            if (!build.isStarted()) {
                build.start();
            } else {
                build.update();
                if (build.getBuildData() == null || (build.getBuildData().isInProgress() && !build.isExpired())) {
                    processingBuildsQueue.add(build);
                } else {
                    finishedBuildsQueue.add(build);
                    if (build.isExpired()) {
                        log.warn("Build for client ({}) is expired.", build.getClientData().getClientId());
                    } else {
                        log.info("Build for client ({}) is processed.", build.getClientData().getClientId());
                    }
                }
            }
        }
    }
}