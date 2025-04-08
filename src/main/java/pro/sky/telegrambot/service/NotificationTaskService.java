package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NotificationTaskService {

    private final NotificationTaskRepository repository;
    private final TelegramBot telegramBot;

    public NotificationTaskService(NotificationTaskRepository repository, TelegramBot telegramBot) {
        this.repository = repository;
        this.telegramBot = telegramBot;
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void sendNotifications() {
        LocalDateTime currentTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        List<NotificationTask> tasks = repository.findByNotificationDateTime(currentTime);

        tasks.forEach(task -> {
            String message = "Напоминание: " + task.getMessage();
            telegramBot.execute(new SendMessage(task.getChatId(), message));
            repository.delete(task);
        });
    }

    public void processReminderMessage(Long chatId, String text) {
        Pattern pattern = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})(\\s+)(.+)");
        Matcher matcher = pattern.matcher(text);

        if (matcher.matches()) {
            handleValidFormat(chatId, matcher.group(1), matcher.group(3));
        } else {
            sendFormatError(chatId);
        }
    }

    private void handleValidFormat(Long chatId, String dateTimeString, String reminderText) {
        try {
            LocalDateTime dateTime = parseDateTime(dateTimeString);
            NotificationTask task = createTask(chatId, reminderText, dateTime);
            repository.save(task);
            sendSuccessResponse(chatId, dateTimeString, reminderText);
        } catch (DateTimeParseException e) {
            sendDateTimeFormatError(chatId);
        }
    }

    private LocalDateTime parseDateTime(String dateTimeString) throws DateTimeParseException {
        return LocalDateTime.parse(dateTimeString,
                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }

    private NotificationTask createTask(Long chatId, String message, LocalDateTime dateTime) {
        NotificationTask task = new NotificationTask();
        task.setChatId(chatId);
        task.setMessage(message);
        task.setNotificationDateTime(dateTime);
        return task;
    }

    private void sendSuccessResponse(Long chatId, String dateTimeString, String reminderText) {
        String response = "Напоминание создано: " + dateTimeString + " - " + reminderText;
        telegramBot.execute(new SendMessage(chatId, response));
    }

    private void sendDateTimeFormatError(Long chatId) {
        String errorMessage = "Неверный формат даты и времени. Используйте формат ДД.ММ.ГГГГ ЧЧ:ММ";
        telegramBot.execute(new SendMessage(chatId, errorMessage));
    }

    private void sendFormatError(Long chatId) {
        String errorMessage = "Неверный формат сообщения. Используйте: ДД.ММ.ГГГГ ЧЧ:ММ Текст напоминания";
        telegramBot.execute(new SendMessage(chatId, errorMessage));
    }
}
