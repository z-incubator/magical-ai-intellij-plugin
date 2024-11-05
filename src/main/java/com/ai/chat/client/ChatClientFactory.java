
package com.ai.chat.client;

import com.ai.chat.AssistantType;
import com.ai.settings.state.GeneralSettings;
import org.springframework.ai.chat.client.ChatClient;

public class ChatClientFactory {

    public ChatClient create(AssistantType type) {
        return create(type, GeneralSettings.getInstance());
    }

    public ChatClient create(AssistantType type, GeneralSettings settings) {
        var chatModel = type.getFamily().createChatModel(settings.getAssistantOptions(type));
        return ChatClient.create(chatModel);
    }
}
