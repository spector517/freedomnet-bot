package ru.borun.freedomnet.bot.stages.impl;

import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.borun.freedomnet.bot.Bot;
import ru.borun.freedomnet.bot.data.StageData;

public class ConfirmConnStage extends DefaultStage {

    @Override
    public void init(Update update, StageData stageData, Bot bot) throws Exception {
        if (update.hasMessage()) {
            var deleteMessage = DeleteMessage.builder()
                    .chatId(update.getMessage().getFrom().getId())
                    .messageId(update.getMessage().getMessageId())
                    .build();
            bot.execute(deleteMessage);

            var client = bot.getClient(update);
            var escapedHost = escapeMD2Chars(client.getHost());
            var escapedLogin = escapeMD2Chars(client.getLogin());
            var escapedPassword = escapeMD2Chars(client.getPassword());
            var text = stageData.getText().formatted(escapedHost, escapedLogin, escapedPassword);
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

    private String escapeMD2Chars(String str) {
        var targetChars = new char[]{'_', '*', '[', ']', '(', ')', '~', '`', '>', '#', '+', '-', '=', '|', '{', '}', '.', '!'};
        var resultSb = new StringBuilder();
        for (char strChar: str.toCharArray()) {
            for (char targetChar: targetChars) {
                if (strChar == targetChar) {
                    resultSb.append('\\');
                    break;
                }
            }
            resultSb.append(strChar);
        }
        return resultSb.toString();
    }
}
