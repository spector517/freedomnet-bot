package ru.borun.freedomnet.bot;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Log4j2
public class ClientService implements Runnable {

    private static ClientService instance;

    private final Queue<Client> processingClientsQueue;

    public static ClientService getInstance() {
        if (instance == null) {
            instance = new ClientService();
        }
        return instance;
    }

    private ClientService() {
        processingClientsQueue = new ConcurrentLinkedQueue<>();
    }

    public void addClientToProcessing(Client client) {
        log.info("Add client '{}' to processing", client.getUser().getId());
        processingClientsQueue.add(client);
    }

    @Override
    @SneakyThrows
    public void run() {
        while(!Thread.interrupted()) {
            var client = processingClientsQueue.poll();
            if (client == null || client.getStage() == null) {
                continue;
            }
            if (!client.isCurrentStageInitiated()) {
                try {
                    client.getStage().init(client.getUpdate());
                } catch (Exception ex) {
                    client.failStage();
                }
                if (!client.isCurrentStageInitiated() || client.getStage().getStageData().isThrough()) {
                    processingClientsQueue.add(client);
                }
                continue;
            }
            if (!client.isCurrentStageCompleted()) {
                try {
                    client.getStage().complete(client.getUpdate());
                } catch (Exception ex) {
                    client.failStage();
                }
                if (client.isCurrentStageCompleted()) {
                    client.nextStage();
                }
                processingClientsQueue.add(client);
            }
        }
    }
}
