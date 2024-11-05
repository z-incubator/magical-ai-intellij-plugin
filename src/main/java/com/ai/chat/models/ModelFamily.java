
package com.ai.chat.models;

import com.ai.settings.state.GeneralSettings;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.util.ReflectionUtils;

import java.util.Arrays;
import java.util.List;

public interface ModelFamily {

    ModelFamily OPEN_AI = new OpenAiModelFamily();

    ModelFamily AZURE_OPENAI = new AzureOpenAiModelFamily();

    ModelFamily ANTHROPIC = new AnthropicModelFamily();

    ModelFamily OLLAMA = new OllamaModelFamily();

    ModelFamily QIANFAN = new QianfanModelFamily();

    ChatModel createChatModel(GeneralSettings.AssistantOptions config);

    String getDefaultApiEndpointUrl();

    default List<String> getCompatibleApiEndpointUrls() {
        return List.of();
    }

    String getApiKeysHomepage();

    default boolean isApiKeyOptional() {
        return "".equals(getApiKeysHomepage());
    }

    static ModelFamily create(Class<? extends ModelFamily> clazz) {
        return Arrays.stream(ModelFamily.class.getFields())
                .filter(field -> field.getType().equals(clazz) && ReflectionUtils.isPublicStaticFinal(field))
                .findFirst()
                .map(field -> {
                    try {
                        return (ModelFamily) field.get(null);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                })
                .orElseGet(() -> {
                    try {
                        return clazz.getConstructor().newInstance();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
