package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.telegram.telegrambots.bots.DefaultBotOptions;


public class CampBot extends TelegramLongPollingBot {

    private static final Logger log = LoggerFactory.getLogger(CampBot.class);

    public CampBot() {
        super(getOptionsWithSocksProxy());
    }

    private static DefaultBotOptions getOptionsWithSocksProxy() {
        DefaultBotOptions options = new DefaultBotOptions();
        options.setProxyHost("127.0.0.1");      // Hiddify mixed port
        options.setProxyPort(12334);            // mixed port Hiddify
        options.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);
        return options;
    }


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
            long chatId = update.getMessage().getChatId();
            log.info("New message from {}: {}", chatId, text);
            try {
                switch (text) {

                    case "/start":
                    case "/help":
                        sendMainMenu(chatId);
                        break;

                    case "📅 План дня":
                        sendPlan(chatId);
                        break;

                    case "🗺 План-сетка":
                        sendPlanSetka(chatId);
                        break;

                    case "🎮 Игры":
                        sendText(chatId, "🎮 Раздел игр (пока в разработке)");
                        break;

                    case "🍽 Столовая":
                        sendText(chatId, "🍽 Меню столовой (пока в разработке)");
                        break;

                    case "📖 Правила":
                        sendText(chatId, "📖 Правила лагеря (пока в разработке)");
                        break;

                    case "📋 Методички":
                        sendText(chatId, "📋 Методички (пока в разработке)");
                        break;

                    default:
                        sendText(chatId, "Не понимаю команду 🤔");
                }
            } catch (Exception e) {
                log.error("Error occurred", e);
                sendError(chatId, e.getMessage());
            }
        }
    }

    private void sendText(long chatId, String text) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        execute(message);
    }

    private void sendMainMenu(long chatId) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("🏕 Выбери раздел:");

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);

        KeyboardRow row1 = new KeyboardRow();
        row1.add("📅 План дня");
        row1.add("🗺 План-сетка");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("🎮 Игры");
        row2.add("🍽 Столовая");

        KeyboardRow row3 = new KeyboardRow();
        row3.add("📖 Правила");
        row3.add("📋 Методички");

        keyboard.setKeyboard(List.of(row1, row2, row3));

        message.setReplyMarkup(keyboard);

        execute(message);
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
            log.info("Sending plan to {}", chatId);
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