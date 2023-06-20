package ru.borun.freedomnet.bot.data;

import lombok.Builder;
import lombok.Data;
import ru.borun.freedomnet.jenkins.data.BuildData;

@Data
@Builder
public class ClientData {
    private int clientId;
    private BuildData buildData;
    private ClientLang lang;
}
