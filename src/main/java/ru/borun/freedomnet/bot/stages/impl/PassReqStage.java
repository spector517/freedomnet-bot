package ru.borun.freedomnet.bot.stages.impl;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.borun.freedomnet.bot.Bot;
import ru.borun.freedomnet.bot.data.StageData;

public class PassReqStage extends DefaultStage {

    @Override
    public void complete(Update update, StageData stageData, Bot bot) throws Exception {
        var client = bot.getClient(update);
        client.setPassword(update.getMessage().getText().trim());
        client.setCurrentStageCompleted(true);
    }
}
