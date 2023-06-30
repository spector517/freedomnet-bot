package ru.borun.freedomnet.bot.stages.impl;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.borun.freedomnet.bot.Bot;
import ru.borun.freedomnet.bot.data.StageData;

public class StopStage extends DefaultStage {

    @Override
    public void init(Update update, StageData stageData, Bot bot) throws Exception {
        super.init(update, stageData, bot);
        bot.removeClient(bot.getClient(update));
    }

    @Override
    public void complete(Update update, StageData stageData, Bot bot) throws Exception {
        bot.getClient(update).setCurrentStageCompleted(true);
    }
}
