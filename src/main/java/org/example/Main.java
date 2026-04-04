package org.example;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    public static void main(String[] args) throws Exception {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);

        // ВАЖНО: отключаем очистку webhook
        System.setProperty("org.telegram.telegrambots.disableWebhook", "true");

        botsApi.registerBot(new Bot());

        System.out.println("Бот запущен!");
    }
}