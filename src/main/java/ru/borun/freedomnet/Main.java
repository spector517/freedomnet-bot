package ru.borun.freedomnet;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.borun.freedomnet.jenkins.BuildService;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Main {
    public static void main(String[] args) {
        var buildProcessingThread = new Thread(BuildService.getInstance());
        buildProcessingThread.setName("Build processing thread");
        buildProcessingThread.start();
    }
}