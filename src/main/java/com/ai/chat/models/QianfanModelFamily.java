
package com.ai.chat.models;

import com.ai.settings.state.GeneralSettings;
import org.springframework.ai.qianfan.QianFanChatModel;
import org.springframework.ai.qianfan.QianFanChatOptions;
import org.springframework.ai.qianfan.api.QianFanApi;

import java.util.List;

public class QianfanModelFamily implements ModelFamily {
    public static final String DEFAULT_BASE_URL = "https://aip.baidubce.com/rpc/2.0/ai_custom/";

    @Override
    public QianFanChatModel createChatModel(GeneralSettings.AssistantOptions config) {
        var baseUrl = config.isEnableCustomApiEndpointUrl() ? config.getApiEndpointUrl() : getDefaultApiEndpointUrl();
        var api = new QianFanApi(baseUrl, config.getAccessKey(), config.getSecretKey());
        var options = QianFanChatOptions.builder()
                .withModel(config.getModelName())
                .withTemperature(config.getTemperature())
                .withTopP(config.getTopP())
                .build();
        return new QianFanChatModel(api, options);
    }

    @Override
    public String getDefaultApiEndpointUrl() {
        return DEFAULT_BASE_URL;
    }

    @Override
    public List<String> getCompatibleApiEndpointUrls() {
        return List.of(DEFAULT_BASE_URL);
    }

    @Override
    public String getApiKeysHomepage() {
        return "https://console.bce.baidu.com/qianfan/ais/console/applicationConsole/application";
    }
}
