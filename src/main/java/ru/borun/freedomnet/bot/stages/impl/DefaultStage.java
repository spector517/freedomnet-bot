package ru.borun.freedomnet.bot.stages.impl;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.borun.freedomnet.bot.Bot;
import ru.borun.freedomnet.bot.data.StageData;
import ru.borun.freedomnet.bot.stages.common.Stagable;

import java.util.List;

public class DefaultStage implements Stagable {

    @Override
    public void init(Update update, StageData stageData, Bot bot) throws Exception {
        var message = SendMessage.builder()
                .text(stageData.getText())
                .parseMode(stageData.getParseMode())
                .chatId(getClientId(update))
                .build();
        if (stageData.getChooses() != null) {
            var inlineKeyboardMarkup = new InlineKeyboardMarkup();
            var buttons = stageData.getChooses().stream().map(choose ->
                    InlineKeyboardButton.builder()
                            .text(choose.getDisplay())
                            .callbackData(choose.getTo())
                            .build()
            ).map(List::of).toList();
            inlineKeyboardMarkup.setKeyboard(buttons);
            message.setReplyMarkup(inlineKeyboardMarkup);
        }
        bot.execute(message);
        bot.getClient(update).setCurrentStageInitiated(true);
    }

    @Override
    public void complete(Update update, StageData stageData, Bot bot) throws Exception {
        if (update.hasCallbackQuery()) {
            for (StageData.Choice choice: stageData.getChooses()) {
                if (update.getCallbackQuery().getData().equals(choice.getTo())) {
                    removeReplyKeyboard(update.getCallbackQuery(), bot);
                    bot.getClient(update).setCurrentStageCompleted(true);
                    break;
                }
            }
        }
    }

    protected void removeReplyKeyboard(CallbackQuery callbackQuery, Bot bot) throws Exception {
        removeReplyKeyboard(
                callbackQuery.getFrom().getId(),
                callbackQuery.getMessage().getMessageId(),
                bot
        );
    }

    protected void removeReplyKeyboard(Message message, Bot bot) throws Exception {
        removeReplyKeyboard(
                message.getChatId(),
                message.getMessageId(),
                bot
        );
    }

    protected long getClientId(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getFrom().getId();
        } else {
            return update.getCallbackQuery().getFrom().getId();
        }
    }

    private void removeReplyKeyboard(long chatId, int messageId, Bot bot) throws TelegramApiException {
        bot.execute(EditMessageReplyMarkup.builder()
                .chatId(chatId)
                .messageId(messageId)
                .build()
        );
    }
}
