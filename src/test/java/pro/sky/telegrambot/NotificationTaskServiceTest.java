package pro.sky.telegrambot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;
import pro.sky.telegrambot.service.NotificationTaskService;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationTaskServiceTest {

    @Mock
    private NotificationTaskRepository repository;

    @Mock
    private TelegramBot telegramBot;

    @InjectMocks
    private NotificationTaskService notificationTaskService;

    @Test
    void processReminderMessage_ValidFormat_ShouldSaveTask() {
        // Arrange
        Long chatId = 123L;
        String validMessage = "01.01.2023 12:00 Test reminder";

        // Act
        notificationTaskService.processReminderMessage(chatId, validMessage);

        // Assert
        verify(repository).save(any(NotificationTask.class));

        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot).execute(messageCaptor.capture());

        SendMessage sentMessage = messageCaptor.getValue();
        assertEquals(chatId, sentMessage.getParameters().get("chat_id"));
        assertTrue(((String)sentMessage.getParameters().get("text")).startsWith("Напоминание создано"));
    }

    @Test
    void processReminderMessage_InvalidDateFormat_ShouldSendError() {
        // Arrange
        Long chatId = 123L;
        String invalidDateMessage = "01/01/2023 12:00 Test reminder";

        // Act
        notificationTaskService.processReminderMessage(chatId, invalidDateMessage);

        // Assert
        verify(repository, never()).save(any());

        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot).execute(messageCaptor.capture());

        SendMessage sentMessage = messageCaptor.getValue();
        assertEquals(chatId, sentMessage.getParameters().get("chat_id"));
        assertTrue(((String)sentMessage.getParameters().get("text")).contains("Неверный формат сообщения"));
    }

    @Test
    void processReminderMessage_InvalidDateTime_ShouldSendDateTimeError() {
        // Arrange
        Long chatId = 123L;
        String invalidDateTimeMessage = "32.01.2023 12:00 Test reminder"; // Несуществующая дата

        // Act
        notificationTaskService.processReminderMessage(chatId, invalidDateTimeMessage);

        // Assert
        verify(repository, never()).save(any());

        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot).execute(messageCaptor.capture());

        SendMessage sentMessage = messageCaptor.getValue();
        assertEquals(chatId, sentMessage.getParameters().get("chat_id"));
        assertTrue(((String)sentMessage.getParameters().get("text")).contains("Неверный формат даты и времени"));
    }
}
