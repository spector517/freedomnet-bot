package ru.borun.freedomnet.bot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.borun.freedomnet.bot.stages.common.Stage;
import ru.borun.freedomnet.bot.stages.common.StageFactory;
import ru.borun.freedomnet.jenkins.Build;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class Client {

    private final User user;
    private Update update;

    private Stage stage;
    private boolean currentStageInitiated;
    private boolean currentStageCompleted;

    private String host;
    private String login;
    private String password;
    private String[] clients;
    private Build build;

    public Client(Update update) {
        this.user = update.hasMessage() ? update.getMessage().getFrom() : update.getCallbackQuery().getFrom();
        this.update = update;
        this.stage = Stage.fromString(StageFactory.INIT_STAGE_NAME);
    }

    public void nextStage() {
        currentStageInitiated = false;
        currentStageCompleted = false;
        if (update.hasCallbackQuery() && !stage.getStageData().isThrough()) {
            stage = Stage.fromString(update.getCallbackQuery().getData());
        } else {
            stage = Stage.fromString(stage.getStageData().getNext());
        }
    }

    public void failStage() {
        currentStageInitiated = false;
        currentStageCompleted = false;
        stage = Stage.fromString(stage.getStageData().getFail());
    }
}
