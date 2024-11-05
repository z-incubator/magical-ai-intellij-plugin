
package com.ai.core;

import com.ai.chat.ConversationContext;
import com.intellij.openapi.components.Service;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;

@Service
public final class ChatCompletionRequestProvider {

    public Prompt chatCompletionRequest(ConversationContext ctx, UserMessage userMessage) {
        ctx.addChatMessage(userMessage);
        return new Prompt(ctx.getChatMessages(ctx.getModelType(), userMessage));
    }
}
