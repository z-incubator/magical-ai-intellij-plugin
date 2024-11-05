
package com.ai.chat;

import com.ai.chat.models.ModelType;
import com.ai.text.TextContent;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.List;

public interface ConversationContext {

    void clear();

    AssistantType getAssistantType();

    List<? extends TextContent> getLastPostedCodeFragments();

    void setLastPostedCodeFragments(List<? extends TextContent> textContents);

    void addChatMessage(Message message);

    ModelType getModelType();

    List<Message> getChatMessages(ModelType model, UserMessage userMessage);
}
