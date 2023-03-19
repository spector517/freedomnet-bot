package ru.borun.freedomnet.bot.data;

import lombok.Data;
import ru.borun.freedomnet.bot.ClientLang;
import ru.borun.freedomnet.jenkins.data.BuildData;

@Data
public class ClientData {
    private int clientId;
    private BuildData buildData;
    private ClientLang lang;
}
