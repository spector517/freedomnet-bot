package ru.borun.freedomnet.bot.stages.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.borun.freedomnet.bot.Bot;
import ru.borun.freedomnet.bot.data.StageData;
import ru.borun.freedomnet.bot.stages.impl.*;

@RequiredArgsConstructor
@Getter
public enum Stage {

    START("start", StageFactory.STAGES_DATA_MAP.get("start"), new DefaultStage()),
    VPN_ADVANTAGES("vpn-advantages", StageFactory.STAGES_DATA_MAP.get("vpn-advantages"), new DefaultStage()),
    ABOUT_PROCEDURE("about-procedure", StageFactory.STAGES_DATA_MAP.get("about-procedure"), new DefaultStage()),
    VPS_RENT("vps-rent", StageFactory.STAGES_DATA_MAP.get("vps-rent"), new DefaultStage()),
    CONN_SUCCESS("conn-success", StageFactory.STAGES_DATA_MAP.get("conn-success"), new DefaultStage()),
    CONN_ERROR("conn-error", StageFactory.STAGES_DATA_MAP.get("conn-error"), new DefaultStage()),
    CLIENTS_RULES("clients-rules", StageFactory.STAGES_DATA_MAP.get("clients-rules"), new DefaultStage()),
    CLIENTS_INCORRECT("clients-incorrect", StageFactory.STAGES_DATA_MAP.get("clients-incorrect"), new DefaultStage()),
    DEPLOY_SUCCESS("deploy-success", StageFactory.STAGES_DATA_MAP.get("deploy-success"), new DefaultStage()),
    DEPLOY_FAIL("deploy-fail", StageFactory.STAGES_DATA_MAP.get("deploy-fail"), new DefaultStage()),

    HOST_REQ("host-req", StageFactory.STAGES_DATA_MAP.get("host-req"), new HostReqStage()),
    LOGIN_REQ("login-req", StageFactory.STAGES_DATA_MAP.get("login-req"), new LoginReqStage()),
    STOP("stop", StageFactory.STAGES_DATA_MAP.get("stop"), new StopStage()),
    PASS_REQ("pass-req", StageFactory.STAGES_DATA_MAP.get("pass-req"), new PassReqStage()),
    CONFIRM_CONN("confirm-conn", StageFactory.STAGES_DATA_MAP.get("confirm-conn"), new ConfirmConnStage()),
    CHECK_CONN("check-conn", StageFactory.STAGES_DATA_MAP.get("check-conn"), new CheckConnStage()),
    CLIENTS_REQ("clients-req", StageFactory.STAGES_DATA_MAP.get("clients-req"), new ClientsReqStage()),
    CLIENTS_CORRECT("clients-correct", StageFactory.STAGES_DATA_MAP.get("clients-correct"), new ClientsCorrectStage()),
    RUN_DEPLOY("run-deploy", StageFactory.STAGES_DATA_MAP.get("run-deploy"), new RunDeployStage());

    public static final Bot bot = Bot.getInstance();

    public static Stage fromString(String name) {
        for (var stage : Stage.values()) {
            if (stage.getName().equals(name)) {
                return stage;
            }
        }
        return null;
    }

    private final String name;
    private final StageData stageData;
    private final Stagable execImpl;

    public void init(Update update) throws Exception{
        execImpl.init(update, stageData, bot);
    }

    public void complete(Update update) throws Exception {
        execImpl.complete(update, stageData, bot);
    }
}
