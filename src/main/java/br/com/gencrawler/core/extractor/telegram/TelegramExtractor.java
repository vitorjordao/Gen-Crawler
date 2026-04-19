package br.com.gencrawler.core.extractor.telegram;

import br.com.gencrawler.core.interfaces.DataExtractor;
import br.com.gencrawler.dto.CrawlRequest;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.updates.GetUpdates;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Telegram extractor for channels and groups.
 *
 * <p>Uses the Bot API. The bot must be a member (or admin for channels) of the target chat
 * to receive messages — the Bot API cannot read historical channel content without being an admin.
 * Messages are pulled via getUpdates (up to ~100 most recent updates in the bot's queue).
 *
 * <p>Selectors supported: text, caption, date, messageId, from.username, from.firstName,
 * chat.id, chat.title.
 */
@Slf4j
@Component
public class TelegramExtractor implements DataExtractor {

    @Value("${telegram.bot.token:}")
    private String botToken;

    private TelegramClient client;

    @PostConstruct
    void init() {
        if (botToken != null && !botToken.isBlank()) {
            this.client = new OkHttpTelegramClient(botToken);
            log.info("Telegram client initialized");
        } else {
            log.warn("Telegram bot token not configured; TelegramExtractor is disabled");
        }
    }

    @Override
    public boolean supports(String sourceType) {
        return "telegram".equalsIgnoreCase(sourceType);
    }

    @Override
    public Map<String, Object> extract(CrawlRequest request) throws Exception {
        if (client == null) {
            throw new IllegalStateException(
                    "Telegram bot token not configured. Set TELEGRAM_BOT_TOKEN environment variable.");
        }

        String channel = extractChannelUsername(request.getSourceUrl());
        log.info("Fetching updates for Telegram channel: @{}", channel);

        List<Update> updates;
        try {
            GetUpdates getUpdates = GetUpdates.builder().limit(100).timeout(0).build();
            updates = client.execute(getUpdates);
        } catch (TelegramApiException e) {
            throw new IllegalStateException("Failed to fetch Telegram updates: " + e.getMessage(), e);
        }

        List<Map<String, Object>> messages = new ArrayList<>();
        for (Update update : updates) {
            Message message = update.getChannelPost() != null
                    ? update.getChannelPost()
                    : update.getMessage();
            if (message == null || message.getChat() == null) {
                continue;
            }
            String messageChannel = message.getChat().getUserName();
            if (messageChannel == null || !messageChannel.equalsIgnoreCase(channel)) {
                continue;
            }

            Map<String, Object> messageData = new HashMap<>();
            for (CrawlRequest.ExtractorConfig config : request.getExtractors()) {
                Object value = extractField(message, config.getSelector());
                if (value != null || Boolean.TRUE.equals(config.getRequired())) {
                    messageData.put(config.getFieldName(), value);
                }
            }
            messages.add(messageData);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("channel", channel);
        result.put("messageCount", messages.size());
        result.put("messages", messages);

        log.info("Extracted {} messages from channel @{}", messages.size(), channel);
        return result;
    }

    @Override
    public int getPriority() {
        return 80;
    }

    private Object extractField(Message message, String field) {
        return switch (field) {
            case "text" -> message.getText();
            case "caption" -> message.getCaption();
            case "date" -> message.getDate();
            case "messageId" -> message.getMessageId();
            case "from.username" -> message.getFrom() != null ? message.getFrom().getUserName() : null;
            case "from.firstName" -> message.getFrom() != null ? message.getFrom().getFirstName() : null;
            case "chat.id" -> message.getChat().getId();
            case "chat.title" -> message.getChat().getTitle();
            default -> null;
        };
    }

    String extractChannelUsername(String url) {
        if (url == null || url.isBlank()) {
            return "";
        }
        String trimmed = url.trim();
        if (trimmed.startsWith("@")) {
            return trimmed.substring(1);
        }
        String[] parts = trimmed.split("/");
        String last = parts[parts.length - 1];
        return last.startsWith("@") ? last.substring(1) : last;
    }
}
