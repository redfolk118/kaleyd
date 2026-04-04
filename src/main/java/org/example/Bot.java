package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class Bot extends TelegramLongPollingBot {

    @Override
    public String getBotUsername() {
        return "kaleydoskop_camp_bot";
    }

    @Override
    public String getBotToken() {
        return "8672163632:AAEuckc9aiDpF6brPGLt7rGgS5pCd2ejW8s";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            String chatId = update.getMessage().getChatId().toString();

            if (text.equals("/start")) {
                sendText(chatId, "Привет! Я бот на Java ☕");
            } else {
                sendText(chatId, "Ты написал: " + text);
            }
        }
    }

    private void sendText(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}