package ru.borun.freedomnet.bot.stages.impl;

import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaDocument;
import ru.borun.freedomnet.bot.Bot;
import ru.borun.freedomnet.bot.data.StageData;
import ru.borun.freedomnet.jenkins.Build;
import ru.borun.freedomnet.jenkins.BuildService;
import ru.borun.freedomnet.jenkins.JenkinsAdapter;
import ru.borun.freedomnet.jenkins.JenkinsConfig;
import ru.borun.freedomnet.jenkins.data.ArtifactData;
import ru.borun.freedomnet.jenkins.data.BuildResults;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Map;

public class RunDeployStage extends DefaultStage {

    private final JenkinsAdapter jenkinsAdapter = JenkinsAdapter.getInstance();
    private final JenkinsConfig jenkinsConfig = JenkinsConfig.getInstance();

    @Override
    public void init(Update update, StageData stageData, Bot bot) throws Exception {
        var job = jenkinsConfig.getWireguardDeployJob();
        var client = bot.getClient(update);
        var jobParams = Map.of(
                "REMOTE_SERVER", client.getHost(),
                "REMOTE_USER", client.getLogin(),
                "REMOTE_PASSWORD", client.getPassword(),
                "CLIENTS", String.join(",", client.getClients()),
                "BUILD_ID", String.valueOf(client.getUser().getId())
        );
        var clientId = update.getCallbackQuery().getFrom().getId();
        client.setBuild(new Build(job, jobParams, jenkinsAdapter, jenkinsConfig, clientId));
        BuildService.getInstance().addBuildToProcessing(client.getBuild());
        super.init(update, stageData, bot);
    }

    @Override
    public void complete(Update update, StageData stageData, Bot bot) throws Exception {
        var client = bot.getClient(update);
        var build = client.getBuild();
        var buildData = build.getBuildData();
        if (build.isStarted() && buildData != null && buildData.getResult() != null && !buildData.getArtifacts().isEmpty()) {
            if (buildData.getResult() != BuildResults.SUCCESS && buildData.getResult() != BuildResults.UNSTABLE) {
                throw new Exception("Build is not success =(");
            }
            if (buildData.getArtifacts().size() > 1) {
                var inputMediaGroup = new ArrayList<InputMedia>();
                for (ArtifactData artifactData : buildData.getArtifacts()) {
                    var artifactBytes = jenkinsAdapter.downloadArtifact(buildData, artifactData);
                    inputMediaGroup.add(
                            InputMediaDocument.builder()
                                    .newMediaStream(new ByteArrayInputStream(artifactBytes))
                                    .mediaName(artifactData.getFileName())
                                    .isNewMedia(true)
                                    .media("attach://%s".formatted(artifactData.getFileName()))
                                    .build()
                    );
                }
                var message = SendMediaGroup.builder()
                        .medias(inputMediaGroup)
                        .chatId(client.getUser().getId())
                        .build();
                bot.execute(message);
            } else {
                var artifactData = buildData.getArtifacts().get(0);
                var artifactBytes = jenkinsAdapter.downloadArtifact(buildData, artifactData);
                var inputFile = new InputFile();
                inputFile.setMedia(new ByteArrayInputStream(artifactBytes), artifactData.getFileName());
                var message = SendDocument.builder()
                        .document(inputFile)
                        .chatId(getClientId(update))
                        .build();
                bot.execute(message);
            }
            client.setCurrentStageCompleted(true);
        }
    }
}
