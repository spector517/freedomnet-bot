package ru.borun.freedomnet.bot.stages.impl;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.borun.freedomnet.bot.Bot;
import ru.borun.freedomnet.bot.data.StageData;

import java.util.Arrays;

public class ClientsCorrectStage extends DefaultStage {

    @Override
    public void init(Update update, StageData stageData, Bot bot) throws Exception {
        var clients = bot.getClient(update).getClients();
        var markedClients = Arrays.stream(clients).map("- *%s*"::formatted).toList();
        var text = stageData.getText().formatted(String.join("\n", markedClients));
        var changedStageData = StageData.builder()
                .text(text)
                .parseMode(stageData.getParseMode())
                .next(stageData.getNext())
                .fail(stageData.getFail())
                .through(stageData.isThrough())
                .chooses(stageData.getChooses())
                .build();

        super.init(update, changedStageData, bot);
    }
}
