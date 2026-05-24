package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private final NotificationTaskRepository notificationTaskRepository;

    @Autowired
    private TelegramBot telegramBot;

    public TelegramBotUpdatesListener(NotificationTaskRepository notificationTaskRepository) {
        this.notificationTaskRepository = notificationTaskRepository;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            if (update.message() != null && update.message().text() != null) {
                String messageText = update.message().text();
                Long chatId = update.message().chat().id();
                if (messageText.equals("/start")) {
                    SendMessage sendMessage = new SendMessage(chatId, "Привет! Я телеграм бот, который будет напоминать тебе о твоих делах!");
                    telegramBot.execute(sendMessage);
                } else {
                    Pattern pattern = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})(\\s+)(.+)");
                    Matcher matcher = pattern.matcher(messageText);
                    if (matcher.matches()) {
                        String dateTimeString = matcher.group(1);
                        String message = matcher.group(3);
                        LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
                        NotificationTask notificationTask = new NotificationTask();
                        notificationTask.setChatId(chatId);
                        notificationTask.setMessage(message);
                        notificationTask.setDateTime(dateTime);
                        notificationTaskRepository.save(notificationTask);
                        SendMessage sendMessage = new SendMessage(chatId, "Напоминание сохранено: " + message);
                        telegramBot.execute(sendMessage);
                    } else {
                        SendMessage sendMessage = new SendMessage(
                                chatId,
                                "Неверный формат. Используйте: ДД.ММ.ГГГГ ЧЧ:ММ Текст напоминания\nПример: 23.05.2026 18:00 Прогулка"
                        );
                        telegramBot.execute(sendMessage);
                    }
                }
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}
