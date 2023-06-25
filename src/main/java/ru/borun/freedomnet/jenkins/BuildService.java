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
        log.info("Add build for client ({}) to processing", build.getClientId());
        processingBuildsQueue.add(build);
    }

    public Build getFinishedBuild() {
        return finishedBuildsQueue.poll();
    }

    @SneakyThrows
    @Override
    public void run() {
        while (!Thread.interrupted()) {
            Thread.sleep(jenkinsConfig.getPollingInterval());
            var build = processingBuildsQueue.poll();
            if (build == null) {
                log.debug("No build in queue, nothing to do.");
                continue;
            }
            if (!build.isStarted()) {
                log.debug("Start build for client '{}'", build.getClientId());
                build.start();
                processingBuildsQueue.add(build);
            } else {
                log.debug("Update build for client '{}'", build.getClientId());
                build.update();
                if (build.getBuildData() == null || (build.getBuildData().isInProgress() && !build.isExpired())) {
                    processingBuildsQueue.add(build);
                } else {
                    finishBuildProcessing(build);
                }
            }
        }
    }

    private void finishBuildProcessing(Build build) {
        finishedBuildsQueue.add(build);
        if (build.isExpired()) {
            log.warn("Build for client '{}' is expired.", build.getClientId());
        } else {
            log.info(
                    "Build for client '{}' is processed. Result is '{}'",
                    build.getClientId(),
                    build.getBuildData().getResult()
            );
        }
    }
}