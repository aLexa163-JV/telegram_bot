package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.repository.NotificationTaskRepository;
import pro.sky.telegrambot.service.NotificationTaskService;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    @Autowired
    private NotificationTaskRepository notificationTaskRepository;

    @Autowired
    private NotificationTaskService notificationTaskService;

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    @Autowired
    private TelegramBot telegramBot;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            if (update.message() != null && update.message().text() != null) {
                Long chatId = update.message().chat().id();
                String text = update.message().text();

                if (text.equals("/start")) {
                    sendWelcomeMessage(chatId);
                } else {
                    notificationTaskService.processReminderMessage(chatId, text);
                }
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
    private void sendWelcomeMessage(Long chatId) {
        String welcomeMessage = "Привет! Я бот для напоминаний. Отправь мне сообщение в формате:\n" +
                "ДД.ММ.ГГГГ ЧЧ:MM Текст напоминания\n" +
                "Например: 01.01.2022 20:00 Сделать домашнюю работу";
        telegramBot.execute(new SendMessage(chatId, welcomeMessage));
    }

}
