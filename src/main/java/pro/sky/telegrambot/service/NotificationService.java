package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import pro.sky.telegrambot.listener.TelegramBotUpdatesListener;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;

@Service
public class NotificationService {
    private Logger logger = LoggerFactory.getLogger(NotificationService.class);
    @Autowired
    private NotificationTaskRepository notificationTaskRepository;

    @Autowired
    private TelegramBot telegramBot;

    @Scheduled(cron = "0 0/1 * * * *")
    public void run() {
        logger.info("Checking tasks is running...");
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        Collection<NotificationTask> notifications = notificationTaskRepository.findByDateTime(dateTime);
        for (NotificationTask notification : notifications) {
            SendMessage message = new SendMessage(notification.getChatId(), "Напоминание: " + notification.getMessage());
            telegramBot.execute(message);
            notificationTaskRepository.delete(notification);
        }
    }
}
