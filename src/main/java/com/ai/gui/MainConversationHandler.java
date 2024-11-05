
package com.ai.gui;

import com.intellij.openapi.application.ApplicationManager;
import com.ai.chat.ChatMessageEvent;
import com.ai.chat.ChatMessageListener;
import com.ai.chat.ConversationContext;
import com.ai.chat.ConversationHandler;
import com.ai.chat.client.ChatHandler;
import com.ai.core.ChatCompletionRequestProvider;
import com.ai.gui.tool.window.ChatPanel;
import reactor.core.scheduler.Schedulers;

public class MainConversationHandler implements ConversationHandler {

    private final ChatPanel chatPanel;

    public MainConversationHandler(ChatPanel chatPanel) {
        this.chatPanel = chatPanel;
    }

    @Override
    public void push(ConversationContext ctx, ChatMessageEvent.Starting event, ChatMessageListener listener) {
        var application = ApplicationManager.getApplication();
        var userMessage = event.getUserMessage();
        var chatCompletionRequestProvider = application.getService(ChatCompletionRequestProvider.class);
        var chatCompletionRequest = chatCompletionRequestProvider.chatCompletionRequest(ctx, userMessage);

        application.getService(ChatHandler.class)
                .handle(ctx, event.initiating(chatCompletionRequest), listener)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }
}
