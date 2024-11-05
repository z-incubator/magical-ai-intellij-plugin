
package com.ai.chat;

import com.intellij.openapi.application.ApplicationInfo;
import com.ai.chat.messages.MessageSupport;
import com.ai.chat.models.ModelType;
import com.ai.core.TextSubstitutor;
import com.ai.text.TextContent;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static com.ai.chat.ChatMessageUtils.countTokens;
import static com.ai.chat.ChatMessageUtils.isRoleSystem;

public class ChatLinkState implements ConversationContext {

    private final LinkedList<Message> chatMessages = new LinkedList<>();
    private volatile List<? extends TextContent> lastSentTextFragments = List.of();
    private volatile TextSubstitutor textSubstitutor = TextSubstitutor.NONE;
    private final AssistantConfiguration configuration;


    public ChatLinkState(AssistantConfiguration configuration) {
        this.configuration = configuration;
    }

    public AssistantConfiguration getModelConfiguration() {
        var configuration = this.configuration;
        if (configuration == null)
            throw new UnsupportedOperationException("ModelConfiguration is not supported by this ChatLink instance");

        return configuration;
    }

    public void setTextSubstitutor(TextSubstitutor textSubstitutor) {
        this.textSubstitutor = Objects.requireNonNull(textSubstitutor);
    }

    public final TextSubstitutor getTextSubstitutor() {
        return textSubstitutor;
    }

    public Supplier<String> getSystemPrompt() {
        return getModelConfiguration().getSystemPrompt();
    }

    @Override
    public List<? extends TextContent> getLastPostedCodeFragments() {
        return lastSentTextFragments;
    }

    @Override
    public void setLastPostedCodeFragments(List<? extends TextContent> textContents) {
        Objects.requireNonNull(textContents);
        this.lastSentTextFragments = textContents;
    }

    @Override
    public void addChatMessage(Message message) {
        synchronized (chatMessages) {
            if (!chatMessages.isEmpty()) {
                Message lastMessage = chatMessages.getLast();
                if (Objects.equals(lastMessage.getMessageType(), message.getMessageType())) {
                    chatMessages.removeLast();
                }
            }
            chatMessages.add(message);
        }
    }

    @Override
    public ModelType getModelType() {
        return getModelConfiguration().getModelType();
    }

    @Override
    public List<Message> getChatMessages(ModelType model, UserMessage userMessage) {
        var chatMessages = new LinkedList<Message>();

        // Add the system prompt appropriately
        boolean hasSystemMessage = addSystemPrompt(model, chatMessages);

        // Add the rest of the messages
        synchronized (this.chatMessages) {
            if (!this.chatMessages.isEmpty())
                chatMessages.addAll(this.chatMessages);

            // Substitute template placeholders
            substitutePlaceholders(chatMessages);

            // Trim messages if exceeding token limit
            int maxTokens = model.getInputTokenLimit();
            int removed = dropOldestMessagesToStayWithinTokenLimit(chatMessages, maxTokens);
            while (removed-- > 0)
                this.chatMessages.remove(hasSystemMessage ? 1 : 0);

            return chatMessages;
        }
    }

    private boolean addSystemPrompt(ModelType model, List<Message> messages) {
        String systemPrompt = createSystemPrompt();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            messages.add(newSystemMessageWithUserMessageFallbackIfUnsupported(model, systemPrompt));
            return true;
        }
        return false;
    }

    private String createSystemPrompt() {
        var systemPrompt = getSystemPrompt().get();
        if (systemPrompt == null || systemPrompt.isBlank())
            return null;

        return systemPrompt.stripTrailing()
                + "\n\nCurrent IDE: " + ApplicationInfo.getInstance().getFullApplicationName()
                + "\nOS: " + System.getProperty("os.name");
    }

    private Message newSystemMessageWithUserMessageFallbackIfUnsupported(ModelType model, String content) {
        if (model.supportsSystemMessage()) {
            return new SystemMessage(content);
        } else {
            return new UserMessage(content);
        }
    }

    public void substitutePlaceholders(List<Message> chatMessages) {
        ChatMessageUtils.substitutePlaceholders(chatMessages, getTextSubstitutor());
    }

    public int dropOldestMessagesToStayWithinTokenLimit(List<Message> messages, int maxTokens) {
        // here we assume ratio at most 2/3 available tokens for input prompt with context history,
        // and at least 1/3 tokens for output
        int tokenLimit = maxTokens / 3 * 2;
        int tokenCount;
        int removed = 0;
        boolean hasSystemMessage = !messages.isEmpty() && isRoleSystem(messages.get(0));
        int oldestMessageIndex = hasSystemMessage ? 1 : 0;

        while ((tokenCount = countTokens(messages)) > tokenLimit && oldestMessageIndex < messages.size() - 1) {
            messages.remove(oldestMessageIndex);
            removed++;
        }

        if (tokenCount > tokenLimit) {
            var lastMessage = messages.get(oldestMessageIndex);
            // TODO: calculation is currently wrong
            var lastMsgCutoff = lastMessage.getContent().length() - tokenLimit;
            if (lastMsgCutoff > 0)
                messages.set(oldestMessageIndex,
                        MessageSupport.substring(lastMessage, lastMsgCutoff));
        }
        return removed;
    }

    @Override
    public AssistantType getAssistantType() {
        return getModelConfiguration().getAssistantType();
    }

    @Override
    public void clear() {
        chatMessages.clear();
        setLastPostedCodeFragments(List.of());
    }
}
