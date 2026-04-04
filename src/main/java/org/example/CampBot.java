package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class CampBot extends TelegramLongPollingBot {

    @Override
    public String getBotUsername() {
        return "kaleydoskop_camp_bot"; // заменить на имя бота
    }

    @Override
    public String getBotToken() {
        return "8672163632:AAEuckc9aiDpF6brPGLt7rGgS5pCd2ejW8s"; // заменить на токен от BotFather
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            try {
                if (text.equalsIgnoreCase("/plan")) {
                    sendPlan(chatId);
                } else if (text.equalsIgnoreCase("/plan_setka")) {
                    sendPlanSetka(chatId);
                } else if (text.equalsIgnoreCase("/start") || text.equalsIgnoreCase("/help")) {
                    sendHelp(chatId);
                }
            } catch (Exception e) {
                sendError(chatId, e.getMessage());
            }
        }
    }

    private void sendHelp(long chatId) {
        try {
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText("Available commands:\n" +
                    "/plan - Get daily plan\n" +
                    "/plan_setka - Get plan grid image\n" +
                    "/help - Show this help message");
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendError(long chatId, String errorMessage) {
        try {
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText("Sorry, an error occurred: " + errorMessage);
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendPlan(long chatId) throws TelegramApiException {
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("plan_day.txt");
            if (inputStream == null) {
                sendError(chatId, "Plan file not found in resources");
                return;
            }
            
            String planText = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText(planText);
            execute(message);
        } catch (Exception e) {
            sendError(chatId, "Failed to read plan: " + e.getMessage());
        }
    }

    private void sendPlanSetka(long chatId) throws TelegramApiException {
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("plan_setka.png");
            if (inputStream == null) {
                sendError(chatId, "Plan grid image not found in resources");
                return;
            }
            
            SendPhoto photo = new SendPhoto();
            photo.setChatId(chatId);
            photo.setPhoto(new InputFile(inputStream, "plan_setka.png"));
            execute(photo);
        } catch (Exception e) {
            sendError(chatId, "Failed to send image: " + e.getMessage());
        }
    }
}