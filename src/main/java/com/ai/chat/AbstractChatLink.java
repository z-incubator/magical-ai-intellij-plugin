package com.ai.chat;

import com.ai.event.ListenerList;

public abstract class AbstractChatLink implements ChatLink {

    protected final ListenerList<ChatMessageListener> chatMessageListeners = ListenerList.of(ChatMessageListener.class);


    @Override
    public void addChatMessageListener(ChatMessageListener listener) {
        chatMessageListeners.addListener(listener);
    }

    @Override
    public void removeChatMessageListener(ChatMessageListener listener) {
        chatMessageListeners.removeListener(listener);
    }
}
