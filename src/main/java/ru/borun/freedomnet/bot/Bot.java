package ru.borun.freedomnet.bot;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.HashMap;
import java.util.Map;

@Log4j2
public class Bot extends TelegramLongPollingBot {

    private static Bot instance;

    private final BotConfig botConfig;
    private final ClientService clientService;
    private final Map<Long, Client> activeClients;

    public static Bot getInstance() {
        if (instance == null) {
            instance = new Bot(BotConfig.getInstance(), ClientService.getInstance());
        }
        return instance;
    }

    @Override
    public String getBotUsername() {
        return botConfig.getName();
    }

    @SneakyThrows
    public void connect() {
        var telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(this);
    }

    @SneakyThrows
    private Bot(BotConfig botConfig, ClientService clientService) {
        super(botConfig.getToken());
        activeClients = new HashMap<>();
        this.botConfig = botConfig;
        this.clientService = clientService;
    }

    @Override
    @SneakyThrows
    public void onUpdateReceived(Update update) {
        var client = getClient(update) != null ? getClient(update) : new Client(update);
        activeClients.put(client.getUser().getId(), client);
        clientService.addClientToProcessing(client);
    }

    public void removeClient(Client client) {
        activeClients.remove(client.getUser().getId());
    }

    public Client getClient(Update update) {
        var user = update.hasMessage() ? update.getMessage().getFrom() : update.getCallbackQuery().getFrom();
        var client = activeClients.get(user.getId());
        if (client != null) {
            client.setUpdate(update);
            return client;
        }
        return null;
    }
}
