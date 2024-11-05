
package com.ai.chat.models;

import com.ai.settings.state.GeneralSettings;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.common.OpenAiApiConstants;

import java.util.List;

public class OpenAiModelFamily implements ModelFamily {

    @Override
    public OpenAiChatModel createChatModel(GeneralSettings.AssistantOptions config) {
        var baseUrl = config.isEnableCustomApiEndpointUrl() ? config.getApiEndpointUrl() : getDefaultApiEndpointUrl();
        var apiKey = config.getApiKey();
        var api = new OpenAiApi(baseUrl, apiKey);
        var options = OpenAiChatOptions.builder()
                .withModel(config.getModelName())
                .withTemperature(config.getTemperature())
                .withStreamUsage(config.isEnableStreamOptions())
                .withTopP(config.getTopP())
                .withN(1)
                .build();
        return new OpenAiChatModel(api, options);
    }

    @Override
    public String getDefaultApiEndpointUrl() {
        return OpenAiApiConstants.DEFAULT_BASE_URL;
    }

    @Override
    public List<String> getCompatibleApiEndpointUrls() {
        return List.of(
                OpenAiApiConstants.DEFAULT_BASE_URL
        );
    }

    @Override
    public String getApiKeysHomepage() {
        return "https://platform.openai.com/api-keys";
    }
}
