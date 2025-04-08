package pro.sky.telegrambot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.sky.telegrambot.listener.TelegramBotUpdatesListener;
import pro.sky.telegrambot.service.NotificationTaskService;

import java.util.Collections;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelegramBotUpdatesListenerTest {

    @Mock
    private TelegramBot telegramBot;

    @Mock
    private NotificationTaskService notificationTaskService;

    @InjectMocks
    private TelegramBotUpdatesListener listener;

    @Test
    void process_StartCommand_ShouldSendWelcomeMessage() {
        // Arrange
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);

        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("/start");
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(123L);

        // Act
        listener.process(Collections.singletonList(update));

        // Assert
        verify(notificationTaskService, never()).processReminderMessage(any(), any());
        verify(telegramBot).execute(any(SendMessage.class));
    }

    @Test
    void process_ReminderMessage_ShouldDelegateToService() {
        // Arrange
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);

        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("01.01.2023 12:00 Test reminder");
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(123L);

        // Act
        listener.process(Collections.singletonList(update));

        // Assert
        verify(notificationTaskService).processReminderMessage(123L, "01.01.2023 12:00 Test reminder");
    }
}