
package com.ai.chat.models;

import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.ChatOptionsBuilder;

public interface ModelType {
    ChatOptions OVERRIDE_NONE = ChatOptionsBuilder.builder().build();

    String id();

    ModelFamily getFamily();

    int getInputTokenLimit();

    default boolean supportsStreaming() {
        return true;
    }

    default boolean supportsSystemMessage() {
        return true;
    }

    default ChatOptions incompatibleChatOptionsOverride() {
        return OVERRIDE_NONE;
    }
}
