package ru.borun.freedomnet.bot.stages.impl;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.borun.freedomnet.bot.Bot;
import ru.borun.freedomnet.bot.data.StageData;
import ru.borun.freedomnet.bot.stages.common.Stagable;
import ru.borun.freedomnet.ssh.ISSHService;
import ru.borun.freedomnet.ssh.SSHService;

public class CheckConnStage implements Stagable {

    private static final int DEFAULT_SSH_PORT = 22;
    private final ISSHService sshService = SSHService.getInstance();

    @Override
    public void init(Update update, StageData stageData, Bot bot) throws Exception {
        var client = bot.getClient(update);
        sshService.checkSSHConnection(client.getHost(), DEFAULT_SSH_PORT, client.getLogin(), client.getPassword());
        bot.getClient(update).setCurrentStageInitiated(true);
    }

    @Override
    public void complete(Update update, StageData stageData, Bot bot) throws Exception {
        bot.getClient(update).setCurrentStageCompleted(true);
    }

}
