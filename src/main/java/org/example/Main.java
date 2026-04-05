package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            System.setProperty("org.telegram.telegrambots.disableWebhook", "true");
            log.info("Starting Telegram bot...");

            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new CampBot());

            log.info("Bot started successfully!");
        } catch (TelegramApiException e) {
            log.error("Failed to start bot", e);
        }
    }
}