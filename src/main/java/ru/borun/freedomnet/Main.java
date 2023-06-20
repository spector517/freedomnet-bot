package ru.borun.freedomnet;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.borun.freedomnet.bot.BotConfig;
import ru.borun.freedomnet.jenkins.BuildService;
import ru.borun.freedomnet.jenkins.JenkinsAdapter;
import ru.borun.freedomnet.jenkins.JenkinsConfig;

import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Main {
    public static void main(String[] args) {
        JenkinsConfig.load(args[0]);
        BotConfig.load(args[0]);
        var buildProcessingThread = startBuildService();
    }

    private static Thread startBuildService() {
        var buildProcessingThread = new Thread(new BuildService(JenkinsConfig.getInstance()));
        buildProcessingThread.setName("Build processing thread");
        buildProcessingThread.start();
        return buildProcessingThread;
    }
}