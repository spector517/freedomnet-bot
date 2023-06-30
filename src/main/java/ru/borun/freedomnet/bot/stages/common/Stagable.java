package ru.borun.freedomnet.bot.stages.common;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.borun.freedomnet.bot.Bot;
import ru.borun.freedomnet.bot.data.StageData;

public interface Stagable {

    void init(Update update, StageData stageData, Bot bot) throws Exception;

    void complete(Update update, StageData stageData, Bot bot) throws Exception;
}
