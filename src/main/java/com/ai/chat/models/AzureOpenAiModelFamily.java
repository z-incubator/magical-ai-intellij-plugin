
package com.ai.chat.models;

import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.ai.settings.state.GeneralSettings;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.ai.azure.openai.AzureOpenAiChatOptions;

public class AzureOpenAiModelFamily implements ModelFamily {

    @Override
    public AzureOpenAiChatModel createChatModel(GeneralSettings.AssistantOptions config) {
        checkConfigurationCompletes(config);

        var baseUrl = config.getAzureApiEndpoint();
        var apiKey = config.getApiKey();
        var api = new OpenAIClientBuilder().credential(new AzureKeyCredential(apiKey))
                .endpoint(baseUrl);
        var options = AzureOpenAiChatOptions.builder()
                .withDeploymentName(config.getAzureDeploymentName())
                .withTemperature(config.getTemperature())
                .withTopP(config.getTopP())
                .withN(1)
                .build();

        return new AzureOpenAiChatModel(api, options);
    }

    private static void checkConfigurationCompletes(GeneralSettings.AssistantOptions config) {
        if (StringUtils.isEmpty(config.getAzureApiEndpoint())) {
            throw new IllegalArgumentException("Azure OpenAI `apiEndpoint` is empty");
        }
        if (StringUtils.isEmpty(config.getApiKey())) {
            throw new IllegalArgumentException("Azure OpenAI `apiKey` is empty");
        }
        if (StringUtils.isEmpty(config.getAzureDeploymentName())) {
            throw new IllegalArgumentException("Azure OpenAI `deploymentName` is empty");
        }
    }

    @Override
    public String getDefaultApiEndpointUrl() {
        return "";
    }

    @Override
    public String getApiKeysHomepage() {
        return "https://portal.azure.com/";
    }
}
