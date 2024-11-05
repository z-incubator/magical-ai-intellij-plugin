
package com.ai.chat;

import com.ai.chat.models.ModelType;

import java.util.function.Supplier;

public interface AssistantConfiguration {

    AssistantType getAssistantType();

    String getModelName();

    ModelType getModelType();

    Supplier<String> getSystemPrompt();

    boolean isEnableStreamResponse();

    default AssistantConfiguration withSystemPrompt(Supplier<String> systemPrompt) {
        return new ConfigurationPageProxy(this) {
            @Override
            public Supplier<String> getSystemPrompt() {
                return systemPrompt;
            }
        };
    }
}
