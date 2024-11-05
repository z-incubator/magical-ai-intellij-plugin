
package com.ai.chat;

@FunctionalInterface
public interface ConversationHandler {

    void push(ConversationContext ctx, ChatMessageEvent.Starting event, ChatMessageListener listener);

}
