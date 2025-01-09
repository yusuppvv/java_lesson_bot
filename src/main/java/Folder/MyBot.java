package Folder;

import lombok.SneakyThrows;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendContact;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static Folder.Status.DONE;
import static Folder.Status.LANGUAGE;

public class MyBot extends TelegramLongPollingBot {
    private static final String MOVIES_XLSX = "Movies.xlsx";
    private static Status status = Status.START; // Global status
    private static Language language;
    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        LocalTime time = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String formattedTime = time.format(formatter);
        Message message = update.getMessage();
        Long chatId = message.getChatId();
        String text = message.getText();
        System.out.println("update.getMessage() = " + update.getMessage());
        if (chatId.equals(-1002486407181L) && update.getMessage().getChat().getType().equals("supergroup")){
            if (update.getMessage().hasVideo() && update.getMessage().getFrom().getId().equals(1206667836L)) {
                if (update.getMessage().getCaption() != null) {
                    String fileId = update.getMessage().getVideo().getFileId();
                    String caption = update.getMessage().getCaption();
                    saveToExcel(fileId , caption);
                    sendReplyMessage(chatId , "Movie added!" , update.getMessage().getMessageId());
                } else if (update.getMessage().getCaption() == null) {
                    String fileId = update.getMessage().getVideo().getFileId();
                    saveToExcel(fileId , null);
                    sendReplyMessage(chatId , "Movie added!" , update.getMessage().getMessageId());
                } else {
                    sendReplyMessage(chatId , update.getMessage().getFrom().getFirstName() + ", Menga faqat kino jo'nata olasiz." , update.getMessage().getMessageId());
                }
            }
        }
        else if (update.getMessage().hasVideo() && !update.getMessage().getFrom().getId().equals(1206667836L)){
            sendContact(chatId , sendSentMessage(chatId , update.getMessage().getFrom().getFirstName() + ", Siz menga kino jo'natish huquqiga ega emasiz. Adminga murojat qiling:") , "Azizbek" , "Yusupov" , "+998997012010");
        }
        switch (status) {
            case START -> {
                if (update.getMessage().hasText() && text.equals("/start")) {
                    sendMessage(chatId, "Assalomu alaykum ! ️ Здравствуйте ! Hello !🙋‍♂️");
                    selectLanguage(chatId);
                    status = LANGUAGE;
                }
            }
            case LANGUAGE -> {
                switch (text) {
                    case "\uD83C\uDDFA\uD83C\uDDFF Uzbek tili" -> {
                        language = Language.uz;
                        status = DONE;
                        sendMessage(chatId, "Assalomu aleykum " + update.getMessage().getChat().getUserName() + ". Bizning botimizga xush kelibsiz!");
                        sendReplyMessage(chatId, "Botimizda faqat kino ko'rishingiz mumkin. Kinolar kod bo'yicha saqlanadi , 1 yoki 2 kabi.", sendSentMessage(chatId, "Botimizdan foydalanish instruksiyasi:"));
                        startUp(chatId, update);
                    }
                    case "🇷🇺 Русский язык" -> {
                        language = Language.ru;
                        status = DONE;
                        sendMessage(chatId, "Здравствуйте " + update.getMessage().getChat().getUserName() + ". Добро пожаловать в наш бот!");
                        sendReplyMessage(chatId, "С нашим ботом вы можете только смотреть фильмы. Фильмы сохраняются по коду, например, 1 или 2.", sendSentMessage(chatId, "Инструкция по использованию нашего бота:"));
                        startUp(chatId, update);
                    }
                    case "\uD83C\uDDFA\uD83C\uDDF8 English language" -> {
                        language = Language.en;
                        status = DONE;
                        sendMessage(chatId, "Hello " + update.getMessage().getChat().getUserName() + ". Welcome to our bot!");
                        sendReplyMessage(chatId, "You can only watch movies with our bot. Movies are saved by code, such as 1 or 2.", sendSentMessage(chatId, "Instruction on how to use our bot:"));
                        startUp(chatId, update);
                    }
                    default -> {
                        sendMessage(chatId, "Iltimos, tildan birini tanlang! 🌐");
                        selectLanguage(chatId);
                    }
                }
            }
            case DONE -> {
                startUp(chatId , update);
            }
        }
    }
    @SneakyThrows
    private void startUp(Long chatId , Update update) {
        if (language == Language.uz) {
            Thread.sleep(500);
            uzbekInterface(chatId , update);
        }
        if (language == Language.ru) {
            Thread.sleep(500);
            russianInterface(chatId , update);
        }
        if (language == Language.en) {
            Thread.sleep(500);
            englishInterface(chatId , update);
        }
    }
    @SneakyThrows
    private void russianInterface(Long chatId, Update update) throws IOException {
        String text = update.getMessage().getText();
        Long user = update.getMessage().getFrom().getId();
        if (update.hasMessage() && update.getMessage().getChat().getId().equals(-1002486407181L)) {
            adminActions(update, user, chatId);
        } else if (text.length() < 3) {
            command(text, chatId, update);
        } else if (text.equals("/manual")) {
            sendReplyMessage(chatId, "С нашим ботом вы можете только смотреть фильмы. Фильмы сохраняются по коду, например, 1 или 2.", sendSentMessage(chatId, "Инструкция по использованию нашего бота:"));
        } else if (text.equals("/help")) {
            sendReplyMessage(chatId, "Если вам нужна помощь в использовании бота, используйте команду /manual.", update.getMessage().getMessageId());
            sendContact(chatId, sendSentMessage(user, "Или свяжитесь с нашим администратором:"), "Azizbek", "Yusupov", "+998997012010");
        }
        else if (text.equals("\uD83C\uDDF7\uD83C\uDDFA Русский язык")){
            sendReplyMessage(chatId , "Вы выбрали русский язык если, вы хотите сменить язык используйте команду /language" , update.getMessage().getMessageId());
        }
        else if (text.equals("/language")) {
            sendMessage(chatId , "Выберите язык 🌐:");
            status = LANGUAGE;
        }
        else {
            error(update , chatId);
        }
    }
    @SneakyThrows
    private void englishInterface(Long chatId, Update update) {
        String text = update.getMessage().getText();
        Long user = update.getMessage().getFrom().getId();
        if (update.hasMessage() && update.getMessage().getChat().getId().equals(-1002486407181L)) {
            adminActions(update, user, chatId);
        }
        else if (text.length() < 3) {
            command(text, chatId, update);
        }
        else if (text.equals("/manual")) {
            sendReplyMessage(chatId, "You can only watch movies with our bot. Movies are saved by code, such as 1 or 2.", sendSentMessage(chatId, "Instruction on how to use our bot:"));
        }
        else if (text.equals("/help")) {
            sendReplyMessage(chatId, "If you need help using the bot, use the /manual command.", update.getMessage().getMessageId());
            sendContact(chatId, sendSentMessage(user, "Or contact our admin:"), "Azizbek", "Yusupov", "+998997012010");
        }
        else if (text.equals("\uD83C\uDDFA\uD83C\uDDF8 English language")){
            sendReplyMessage(chatId , "You chose english language. If you want to change language type /language command!" , update.getMessage().getMessageId());
        }
        else if (text.equals("/language")) {
            sendMessage(chatId , "Select your language 🌐:");
            status = LANGUAGE;
        }
        else {
            error(update , chatId);
        }
    }
    @SneakyThrows
    private void uzbekInterface(Long chatId , Update update) throws IOException {
        String text = update.getMessage().getText();
        Long user = update.getMessage().getFrom().getId();
        if (update.hasMessage() && update.getMessage().getChat().getId().equals(-1002486407181L)) {
            adminActions(update , user , chatId);
        }
        else if (text.length() < 3) {
            command(text , chatId , update);
        }
        else if (text.equals("/manual")) {
            sendReplyMessage(chatId, "Botimizda faqat kino ko'rishingiz mumkin. Kinolar kod bo'yicha saqlanadi , 1 yoki 2 kabi." , sendSentMessage(chatId, "Botimizdan foydalanish instruksiyasi:"));
        }
        else if (text.equals("/help")) {
            sendReplyMessage(chatId , "Agar sizga bot bilan foydalanishda yordam kerak bo'lsa /manual komandasini ishlating." , update.getMessage().getMessageId());
            sendContact(chatId , sendSentMessage(user , "Yoki bizning admin bilan muloqat qiling:") , "Azizbek" , "Yusupov" , "+998997012010");
        }
        else if (text.equals("\uD83C\uDDFA\uD83C\uDDFF Uzbek tili")){
            sendReplyMessage(chatId , "Siz uzbek tilini tanladingiz. Tilni o'zgartirish uchun /language komandasini ishlating!" , update.getMessage().getMessageId());
        }
        else if (text.equals("/language")) {
            sendMessage(chatId , "Til tanlang 🌐:");
            status = LANGUAGE;
        }
        else {
            error(update , chatId);
        }
    }
    private void selectLanguage(Long chatId) {
        sendReplyKeyboardMessage(chatId, "Tilingizni tanlang: 🌐", languageButton());
    }
    private ReplyKeyboardMarkup languageButton() {
        KeyboardButton uz = new KeyboardButton("\uD83C\uDDFA\uD83C\uDDFF Uzbek tili");
        KeyboardButton ru = new KeyboardButton("🇷🇺 Русский язык");
        KeyboardButton en = new KeyboardButton("\uD83C\uDDFA\uD83C\uDDF8 English language");
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        row1.add(uz);
        row1.add(ru);
        row2.add(en);
        List<KeyboardRow> rows = new ArrayList<>();
        rows.add(row1);
        rows.add(row2);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        replyKeyboardMarkup.setKeyboard(rows);
        return replyKeyboardMarkup;
    }
    private void sendReplyKeyboardMessage(Long chatId, String text, ReplyKeyboardMarkup keyboardMarkup) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    @SneakyThrows
    private Integer sendSentMessage(Long user, String s) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user);
        sendMessage.setText(s);
        Message execute = execute(sendMessage);
        return execute.getMessageId();
    }
    @SneakyThrows
    private void sendContact(Long chatId, Integer messageId, String firstName, String lastName, String number) {
        SendContact sendContact = new SendContact();
        sendContact.setChatId(chatId);
        sendContact.setReplyToMessageId(messageId);
        sendContact.setFirstName(firstName);
        sendContact.setLastName(lastName);
        sendContact.setPhoneNumber(number);
        execute(sendContact);
    }
    private void error(Update update , Long chatId) throws InterruptedException {
        switch (language) {
            case uz -> {
                sendReplyMessage(chatId , "Noto'g'ri komanda!" , update.getMessage().getMessageId());
                Thread.sleep(1000);
                sendReplyMessage(chatId, "Botimizda faqat kino ko'rishingiz mumkin. Kinolar kod bo'yicha saqlanadi , 1 yoki 2 kabi." , sendSentMessage(chatId, "Botimizdan foydalanish instruksiyasi:"));
            }
            case ru -> {
                sendReplyMessage(chatId, "Неверная команда!", update.getMessage().getMessageId());
                Thread.sleep(1000);
                sendReplyMessage(chatId, "С нашим ботом вы можете только смотреть фильмы. Фильмы сохраняются по коду, например, 1 или 2.", sendSentMessage(chatId, "Инструкция по использованию нашего бота:"));
            }
            case en -> {
                sendReplyMessage(chatId, "Invalid command!", update.getMessage().getMessageId());
                Thread.sleep(1000);
                sendReplyMessage(chatId, "With our bot, you can only watch movies. Movies are stored by code, such as 1 or 2.", sendSentMessage(chatId, "Instructions for using our bot:"));
            }
        }
    }
    @SneakyThrows
    private void command(String text, Long chatId, Update update) {
        switch (language) {
            case uz -> {
                if (text.matches("\\d+")) {
                    try {
                        int id = Integer.parseInt(text);
                        String videoUrl = getVideoById(id - 1);
                        String caption = getCaption(id - 1);
                        sendVideo(chatId, videoUrl, caption, text, update.getMessage().getMessageId());
                        sendReplyKeyboardMessage(chatId , "Keyingi darsni ko'rishni xohlaysizmi?" , nextVideoButton());
                        nextVideo(id , text , chatId , update);
                    } catch (Exception e) {
                        sendMessage(chatId, "Hozircha bu raqamga tegishli video yo'q 😟!");
                    }
                } else {
                    sendMessage(chatId, "Iltimos, faqat raqamli buyruq yuboring, masalan 1 yoki 2.");
                }
            }
            case ru -> {
                if (text.matches("\\d+")) {
                    try {
                        int id = Integer.parseInt(text);
                        String videoUrl = getVideoById(id - 1);
                        String caption = getCaption(id - 1);
                        sendVideo(chatId, videoUrl, caption, text, update.getMessage().getMessageId());
                    } catch (Exception e) {
                        sendMessage(chatId, "Видео с этим номером еще нет 😟!");
                    }
                } else {
                    sendMessage(chatId, "Пожалуйста, отправьте только числовую команду, например 1 или 2.");
                }
            }
            case en -> {
                if (text.matches("\\d+")) {
                    try {
                        int id = Integer.parseInt(text);
                        String videoUrl = getVideoById(id - 1);
                        String caption = getCaption(id - 1);
                        sendVideo(chatId, videoUrl, caption, text, update.getMessage().getMessageId());
                    } catch (Exception e) {
                        sendMessage(chatId, "There is no video with this number yet 😟!");
                    }
                }
                else {
                    sendMessage(chatId, "Please send a numeric command only, like 1 or 2.");
                }
            }
        }
    }

    private void nextVideo(int id , String text , Long chatId , Update update) throws IOException {
        if (text.equals("⏭ Keyingi darsni ko'rish")) {
            String videoId = getVideoById(id);
            String captin = getCaption(id);
            sendVideo(chatId, videoId, captin, text + 1, update.getMessage().getMessageId());
            sendReplyKeyboardMessage(chatId, "Keyingi darsni ko'rishni xohlaysizmi?", nextVideoButton());
        } else if (text.equals("⏮ Xohlagan darsni ko'rish")) {
            sendReplyMessage(chatId, "Xohlagan darsingizni ko'rishingiz mumkin😊", update.getMessage().getMessageId());
            command(null, chatId, update);
        }
    }

    private ReplyKeyboardMarkup nextVideoButton() {
        KeyboardButton button = new KeyboardButton("⏭ Keyingi darsni ko'rish");
        KeyboardButton button1 = new KeyboardButton("⏮ Xohlagan darsni ko'rish");
        KeyboardRow row1 = new KeyboardRow();
        row1.add(button);
        KeyboardRow row2 = new KeyboardRow();
        row2.add(button1);
        List<KeyboardRow> rows = new ArrayList<>();
        rows.add(row1);
        rows.add(row2);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        replyKeyboardMarkup.setKeyboard(rows);
        replyKeyboardMarkup.setResizeKeyboard(true);
        return replyKeyboardMarkup;
    }
    private void adminActions(Update update , Long admin , Long chatId) throws IOException {
        if (update.getMessage().hasVideo()) {
            if (update.getMessage().getCaption() != null) {
                String fileId = update.getMessage().getVideo().getFileId();
                String caption = update.getMessage().getCaption();
                saveToExcel(fileId , caption);
                sendReplyMessage(chatId , "Movie added! 🎥" , update.getMessage().getMessageId());
            } else  {
                String fileId = update.getMessage().getVideo().getFileId();
                saveToExcel(fileId , null);
                sendReplyMessage(chatId , "Movie Added! 🎥" , update.getMessage().getMessageId());
            }
        }
        else {
            sendMessage(chatId, update.getMessage().getFrom().getFirstName() + " , Iltimos menga faqat kino yuboring!");
        }
    }
    @SneakyThrows
    private void sendReplyMessage(Long chatId, String s, Integer messageId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setReplyToMessageId(messageId);
        sendMessage.setChatId(chatId);
        sendMessage.setText(s);
        execute(sendMessage);
    }
    @SneakyThrows
    private void sendVideo(Long chatId, String videoById , String caption, String text , Integer messageId) {
        switch (language) {
            case uz -> {
                SendVideo sendVideo = new SendVideo();
                sendVideo.setChatId(chatId);
                sendVideo.setReplyToMessageId(messageId);
                sendVideo.setVideo(new InputFile(videoById));
                sendVideo.setCaption("Kino kodi: " + text + "\nUning tavsifi: " + "\n" + caption);
                sendVideo.setProtectContent(true);
            }
            case ru -> {
                SendVideo sendVideo = new SendVideo();
                sendVideo.setChatId(chatId);
                sendVideo.setReplyToMessageId(messageId);
                sendVideo.setCaption(caption);
                sendVideo.setVideo(new InputFile(videoById));
                sendVideo.setCaption("Код фильма: " + text + "\nОписания фильма: " + "\n" + caption);
                sendVideo.setProtectContent(true);
                execute(sendVideo);
            }
            case en -> {
                SendVideo sendVideo = new SendVideo();
                sendVideo.setChatId(chatId);
                sendVideo.setReplyToMessageId(messageId);
                sendVideo.setCaption(caption);
                sendVideo.setVideo(new InputFile(videoById));
                sendVideo.setCaption("Movie code: " + text + "\nDescription: " + "\n" + caption);
                execute(sendVideo);
            }
        }
    }
    private String getVideoById(int ID) throws IOException {
        Workbook workbook;
        Sheet sheet;
        File file = new File(MOVIES_XLSX);
        FileInputStream fis = new FileInputStream(file);
        workbook = new XSSFWorkbook(fis);
        sheet = workbook.getSheetAt(0);
        fis.close();
        Row row = sheet.getRow(ID);
        return row.getCell(1).getStringCellValue();
    }
    private String getCaption(int ID) throws IOException {
        Workbook workbook;
        Sheet sheet;
        File file = new File(MOVIES_XLSX);
        FileInputStream fis = new FileInputStream(file);
        workbook = new XSSFWorkbook(fis);
        sheet = workbook.getSheetAt(0);
        fis.close();
        Row row = sheet.getRow(ID);
        return row.getCell(2).getStringCellValue();
    }
    @SneakyThrows
    private void sendMessage(Long chatId, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        execute(sendMessage);
    }
    private void saveToExcel(String id , String caption) throws IOException {
        Workbook workbook;
        Sheet sheet;
        File file = new File(MOVIES_XLSX);
        if (file.exists()) {
            FileInputStream fis = new FileInputStream(file);
            workbook = new XSSFWorkbook(fis);
            sheet = workbook.getSheetAt(0);
            fis.close();
        }
        else {
            workbook = new XSSFWorkbook();
            sheet = workbook.createSheet("Videos");
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Row Number");
            headerRow.createCell(1).setCellValue("Video ID");
            headerRow.createCell(2).setCellValue("Caption");
        }
        int lastRowNum = sheet.getLastRowNum();
        Row newRow = sheet.createRow(lastRowNum + 1);
        newRow.createCell(0).setCellValue(lastRowNum + 2);
        newRow.createCell(1).setCellValue(id);
        newRow.createCell(2).setCellValue(caption);
        FileOutputStream fos = new FileOutputStream(MOVIES_XLSX);
        workbook.write(fos);
        fos.close();
        workbook.close();
    }
    @Override
    public String getBotUsername() {
        return "https://t.me/java_lesson_pdp_bot";
    }
    @Override
    public String getBotToken() {
        return "7164890679:AAHu_zJ7d6Ii4sPDp7LNKbwPfM0L1HpRmTQ";
    }
}