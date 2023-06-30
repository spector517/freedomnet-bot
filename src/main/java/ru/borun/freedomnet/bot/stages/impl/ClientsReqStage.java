package ru.borun.freedomnet.bot.stages.impl;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.borun.freedomnet.bot.Bot;
import ru.borun.freedomnet.bot.data.StageData;

import java.util.regex.Pattern;

public class ClientsReqStage extends DefaultStage {

    @Override
    public void complete(Update update, StageData stageData, Bot bot) throws Exception {
        var client = bot.getClient(update);
        var receivedClients = update.getMessage().getText().trim().split("\\s");
        if (receivedClients.length > 10) {
            throw new Exception("Too many clients.");
        }
        var pattern = Pattern.compile("^[A-Za-z][A-z0-9]{0,15}$");
        for (String receivedClient: receivedClients) {
            if (!pattern.matcher(receivedClient).matches()) {
                throw new Exception("Error client naming '%s'".formatted(receivedClient));
            }
        }
        client.setClients(receivedClients);
        client.setCurrentStageCompleted(true);
    }
}
