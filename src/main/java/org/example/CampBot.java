package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.telegram.telegrambots.bots.DefaultBotOptions;


public class CampBot extends TelegramLongPollingBot {

    private static final Logger log = LoggerFactory.getLogger(CampBot.class);
    private static final String USERS_FILE = "users.txt";
    private Set<Long> allChatIds = new HashSet<>();
    public CampBot() {
        loadUsers();
//        super(getOptionsWithSocksProxy());
    }

//    private static DefaultBotOptions getOptionsWithSocksProxy() {
//        DefaultBotOptions options = new DefaultBotOptions();
//        options.setProxyHost("127.0.0.1");
//        options.setProxyPort(12334);
//        options.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);
//        return options;
//    }


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
            long chatId = update.getMessage().getChatId();
            saveUser(chatId);
            String text = update.getMessage().getText();
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
                        sendText(chatId, "🎮 потом");
                        break;

                    case "🍽 Столовая":
                        sendCanteen(chatId);
                        break;

                    case "📞 Звонилка":
                        sendText(chatId, "📖 отдых");
                        break;

                    case "📋 Дежурства":
                        sendDuty(chatId);
                        break;


                    case "/refresh":
                        broadcastMainMenu();
                        sendText(chatId, "refresh stat ok");
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

    private void loadUsers() {
        log.info("Loading users from {}", USERS_FILE);
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            log.info("Users file not found, starting with empty user set");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                allChatIds.add(Long.parseLong(line.trim()));
            }
            log.info("Loaded {} users", allChatIds.size());
        } catch (Exception e) {
            log.error("Error loading users", e);
        }
    }

    private void saveUser(long chatId) {
        if (allChatIds.add(chatId)) {
            log.info("New user saved: {}", chatId);
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(USERS_FILE, true))) {
                bw.write(String.valueOf(chatId));
                bw.newLine();
            } catch (IOException e) {
                log.error("Error saving user {}", chatId, e);
            }
        }
    }

    private void broadcastMainMenu() {
        log.info("Broadcasting main menu to {} users", allChatIds.size());
        int successCount = 0;
        int failCount = 0;
        for (Long chatId : allChatIds) {
            try {
                sendMainMenu(chatId);
                sendText(chatId, "refresh stat ok");
                successCount++;
            } catch (TelegramApiException e) {
                log.error("Failed to broadcast to {}", chatId, e);
                failCount++;
            }
        }
        log.info("Broadcast completed: {} succeeded, {} failed", successCount, failCount);
    }

    private void sendText(long chatId, String text) throws TelegramApiException {
        log.debug("Sending text to {}: {}", chatId, text);
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        execute(message);
    }

    private void sendMainMenu(long chatId) throws TelegramApiException {
        log.debug("Sending main menu to {}", chatId);
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
        row3.add("📞 Звонилка");
        row3.add("📋 Дежурства");

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
        log.info("Sending plan to {}", chatId);
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("plan_day.txt");
            if (inputStream == null) {
                log.error("Plan file not found in resources");
                sendError(chatId, "Plan file not found in resources");
                return;
            }
            
            String planText = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText(planText);
            execute(message);
            log.info("Plan sent successfully to {}", chatId);
        } catch (Exception e) {
            log.error("Failed to send plan to {}", chatId, e);
            sendError(chatId, "Failed to read plan: " + e.getMessage());
        }
    }

    private void sendPlanSetka(long chatId) throws TelegramApiException {
        log.info("Sending plan grid to {}", chatId);
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("plan_setka.png");
            if (inputStream == null) {
                log.error("Plan grid image not found in resources");
                sendError(chatId, "Plan grid image not found in resources");
                return;
            }
            sendText(chatId, "секунду, отправляю");

            SendPhoto photo = new SendPhoto();
            photo.setChatId(chatId);
            photo.setPhoto(new InputFile(inputStream, "plan_setka.png"));
            execute(photo);
            log.info("Plan grid sent successfully to {}", chatId);
        } catch (Exception e) {
            log.error("Failed to send plan grid to {}", chatId, e);
            sendError(chatId, "Failed to send image: " + e.getMessage());
        }
    }

    private void sendCanteen(long chatId) throws TelegramApiException {
        log.info("Sending canteen image to {}", chatId);
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("canteen.png");
            if (inputStream == null) {
                log.error("Canteen image not found in resources");
                sendError(chatId, "Canteen image not found in resources");
                return;
            }
            sendText(chatId, "секунду, отправляю");

            SendPhoto photo = new SendPhoto();
            photo.setChatId(chatId);
            photo.setPhoto(new InputFile(inputStream, "canteen.png"));
            execute(photo);
            log.info("Canteen image sent successfully to {}", chatId);
        } catch (Exception e) {
            log.error("Failed to send canteen image to {}", chatId, e);
            sendError(chatId, "Failed to send image: " + e.getMessage() + "send exception to @redfolk118");
        }
    }

    private void sendDuty(long chatId) throws TelegramApiException {
        log.info("Sending duty image to {}", chatId);
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("duty.png");
            if (inputStream == null) {
                log.error("Duty image not found in resources");
                sendError(chatId, "Duty image not found in resources");
                return;
            }
            sendText(chatId, "секунду, отправляю");

            SendPhoto photo = new SendPhoto();
            photo.setChatId(chatId);
            photo.setPhoto(new InputFile(inputStream, "duty.png"));
            execute(photo);
            log.info("Duty image sent successfully to {}", chatId);
        } catch (Exception e) {
            log.error("Failed to send duty image to {}", chatId, e);
            sendError(chatId, "Failed to send image: " + e.getMessage() + "send exception to @redfolk118");
        }
    }

}