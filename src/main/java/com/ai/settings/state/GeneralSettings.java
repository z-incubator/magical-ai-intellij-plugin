
package com.ai.settings.state;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.serviceContainer.NonInjectable;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Tag;
import com.intellij.util.xmlb.annotations.Transient;
import com.ai.chat.AssistantConfiguration;
import com.ai.chat.AssistantType;
import com.ai.chat.models.CustomModel;
import com.ai.chat.models.ModelType;
import com.ai.chat.models.StandardModel;
import com.ai.settings.CustomAction;
import com.ai.settings.auth.CredentialStore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

import static com.ai.chat.AssistantType.System.*;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;

/**
 * 系统配置
 */
@Getter
@Setter
@State(name = "settings.com.ai.OpenAISettingsState",
        storages = @Storage("MagicalAIAISettings.xml"))
@Tag("AISettings")
public class GeneralSettings implements PersistentStateComponent<GeneralSettings> {

    public static final String BASE_PROMPT = "You are a professional software engineer, language: Chinese; source code language: English";

    public static final List<AssistantType.System> DEFAULT_ENABLED_SYSTEMS = List.of(OpenAI);

    public String defaultModel = Qianfan.name();

    private volatile String readTimeout = "50000";
    private volatile boolean enableAvatar = true;
    private volatile Boolean enableInitialMessage = true;

    private volatile AssistantOptions openAIConfig;
    private volatile AssistantOptions azureOpenAiConfig;
    private volatile AssistantOptions claudeConfig;
    private volatile AssistantOptions ollamaConfig;
    private volatile AssistantOptions qianfanConfig;

    private volatile String onlineUrl = "https://yiyan.baidu.com/";

    private volatile List<CustomAction> customActionsPrefix = new CopyOnWriteArrayList<>();

    public String roleText = BASE_PROMPT;

    public static GeneralSettings getInstance() {
        return ApplicationManager.getApplication().getService(GeneralSettings.class);
    }

    public GeneralSettings() {
        this((CredentialStore) null);
    }

    @NonInjectable
    protected GeneralSettings(CredentialStore credStore) {
        setOpenAIConfig(AssistantOptions.forAssistantType(OpenAI, credStore, StandardModel.GPT_4_O.id()));
        setAzureOpenAiConfig(AssistantOptions.forAssistantType(AzureOpenAI, credStore, StandardModel.GPT_4.id()));
        setClaudeConfig(AssistantOptions.forAssistantType(Claude, credStore));
        setOllamaConfig(AssistantOptions.forAssistantType(Ollama, credStore, "llama3"));
        setQianfanConfig(AssistantOptions.forAssistantType(Qianfan, credStore, StandardModel.ERNIE_4_0_8K_Preview.id()));
    }

    public void setOpenAIConfig(AssistantOptions openAIConfig) {
        this.openAIConfig = openAIConfig;
        this.openAIConfig.setAssistantType(OpenAI);
    }

    public void setAzureOpenAiConfig(AssistantOptions azureOpenAiConfig) {
        this.azureOpenAiConfig = azureOpenAiConfig;
        this.azureOpenAiConfig.setAssistantType(AssistantType.System.AzureOpenAI);
    }

    public void setClaudeConfig(AssistantOptions claudeConfig) {
        this.claudeConfig = claudeConfig;
        this.claudeConfig.setAssistantType(AssistantType.System.Claude);
    }

    public void setOllamaConfig(AssistantOptions ollamaConfig) {
        this.ollamaConfig = ollamaConfig;
        this.ollamaConfig.setAssistantType(AssistantType.System.Ollama);
    }

    public void setQianfanConfig(AssistantOptions qianfanConfig) {
        this.qianfanConfig = qianfanConfig;
        this.qianfanConfig.setAssistantType(AssistantType.System.Qianfan);
    }

    public Set<AssistantType.System> selectedModel() {
        return Set.of(valueOf(defaultModel));
    }

    @Transient
    public AssistantOptions getAssistantOptions(AssistantType assistantType) {
        if (!(assistantType instanceof AssistantType.System system))
            throw new IllegalArgumentException("Invalid Assistant Type: " + assistantType);

        return switch (system) {
            case OpenAI -> openAIConfig;
            case AzureOpenAI -> azureOpenAiConfig;
            case Claude -> claudeConfig;
            case Ollama -> ollamaConfig;
            case Qianfan -> qianfanConfig;
            case OnlineTool -> throw new IllegalArgumentException("Invalid Assistant Type: " + assistantType);
        };
    }

    @Nullable
    @Override
    public GeneralSettings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull GeneralSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    @Getter
    @Setter
    @Tag("ApiConfig")
    public static class AssistantOptions implements AssistantConfiguration {

        // 存储凭证的地方
        private final @Getter(AccessLevel.NONE) CredentialStore credentialStore;

        // 助手类型
        private volatile AssistantType assistantType;

        // 模型名称
        private volatile String modelName;

        // API 密钥（掩码）
        private volatile String apiKeyMasked = "";

        // 温度
        private volatile double temperature = 0.4;

        // top_p 参数
        private volatile double topP = 0.95;

        // 启用令牌消耗
        private volatile boolean enableTokenConsumption = true;

        // 启用流式响应
        private volatile boolean enableStreamResponse = true;

        // 启用流式选项
        private volatile boolean enableStreamOptions = true;

        // 启用自定义 API 端点 URL
        private volatile boolean enableCustomApiEndpointUrl = false;

        // API 端点 URL
        private volatile String apiEndpointUrl = "";

        // Azure API 端点
        private volatile String azureApiEndpoint = "";

        // Azure 部署名称
        private volatile String azureDeploymentName = "";

        // API 端点 URL 历史记录
        private volatile List<String> apiEndpointUrlHistory = List.of(apiEndpointUrl);

        // 自定义模型列表
        private volatile List<CustomModel> apiModels = List.of();

        // 访问密钥
        private volatile String accessKey = "";

        // 秘密密钥
        private volatile String secretKey = "";

        public AssistantOptions() {
            this((CredentialStore) null);
        }

        @NonInjectable
        private AssistantOptions(CredentialStore credStore) {
            this.credentialStore = credStore;
        }

        // 根据助手类型创建选项
        public static AssistantOptions forAssistantType(AssistantType assistantType, CredentialStore credStore) {
            var firstModel = StandardModel.findFirstAvailableModelInFamily(assistantType.getFamily())
                    .map(ModelType::id).orElse(null);
            return forAssistantType(assistantType, credStore, firstModel);
        }

        // 根据助手类型和模型名称创建选项
        public static AssistantOptions forAssistantType(AssistantType assistantType, CredentialStore credStore, String modelName) {
            var options = new AssistantOptions(credStore);
            options.setAssistantType(assistantType);
            options.setApiEndpointUrl(assistantType.getFamily().getDefaultApiEndpointUrl());
            options.setModelName(modelName);
            return options;
        }

        // 设置助手类型
        public void setAssistantType(AssistantType assistantType) {
            this.assistantType = assistantType;
            if ("".equals(getApiEndpointUrl())) {
                setApiEndpointUrl(assistantType.getFamily().getDefaultApiEndpointUrl());
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                    assistantType.name(),
                    modelName,
                    apiKeyMasked,
                    accessKey,
                    secretKey,
                    temperature,
                    topP,
                    enableTokenConsumption,
                    enableStreamResponse,
                    enableStreamOptions,
                    enableCustomApiEndpointUrl,
                    apiEndpointUrl,
                    azureApiEndpoint,
                    azureDeploymentName,
                    defaultIfNull(apiModels, List.of())
            );
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof AssistantOptions that) {
                return Objects.equals(assistantType, that.assistantType) &&
                        Objects.equals(modelName, that.modelName) &&
                        Objects.equals(apiKeyMasked, that.apiKeyMasked) &&
                        Objects.equals(accessKey, that.accessKey) &&
                        Objects.equals(secretKey, that.secretKey) &&
                        Objects.equals(temperature, that.temperature) &&
                        Objects.equals(topP, that.topP) &&
                        Objects.equals(enableTokenConsumption, that.enableTokenConsumption) &&
                        Objects.equals(enableStreamResponse, that.enableStreamResponse) &&
                        Objects.equals(enableStreamOptions, that.enableStreamOptions) &&
                        Objects.equals(enableCustomApiEndpointUrl, that.enableCustomApiEndpointUrl) &&
                        Objects.equals(apiEndpointUrl, that.apiEndpointUrl) &&
                        Objects.equals(azureApiEndpoint, that.azureApiEndpoint) &&
                        Objects.equals(azureDeploymentName, that.azureDeploymentName) &&
                        Objects.equals(defaultIfNull(apiModels, List.of()), defaultIfNull(that.apiModels, List.of()));
            }
            return false;
        }

        @Override
        @Transient
        public AssistantType getAssistantType() {
            return assistantType;
        }

        // 获取模型类型
        @Transient
        public ModelType getModelType() {
            String modelName = getModelName();
            try {
                return StandardModel.of(modelName);
            } catch (IllegalArgumentException e) {
                var customModels = getApiModels();
                if (customModels != null) {
                    for (var model : customModels) {
                        if (modelName.equals(model.id())) {
                            return model;
                        }
                    }
                }
                return new CustomModel(modelName, assistantType.getFamily(), Integer.MAX_VALUE);
            }
        }

        @Override
        public Supplier<String> getSystemPrompt() {
            return () -> "";
        }

        // 获取当前凭证存储
        private CredentialStore credentialStore() {
            return (credentialStore != null) ? credentialStore : CredentialStore.systemCredentialStore();
        }

        // 获取 API 密钥
        @Transient
        public String getApiKey() {
            CredentialStore currStore = credentialStore();
            CredentialStore systemStore = CredentialStore.systemCredentialStore();
            var apiKey = currStore.getPassword(getAssistantType().name());
            if (apiKey == null && currStore != systemStore) {
                apiKey = systemStore.getPassword(getAssistantType().name());
            }
            return (apiKey != null) ? apiKey : "";
        }

        // 设置 API 密钥
        public void setApiKey(String apiKey) {
            setApiKeyMasked(maskText(defaultIfEmpty(
                    credentialStore().setAndGetPassword(getAssistantType().name(), apiKey), "")));
        }

        // 掩码文本
        private static String maskText(String text) {
            final int maskStart = 3;
            final int maskEnd = 4;
            return (text.length() <= maskStart + maskEnd) ? text : text.substring(0, maskStart) + "***" + text.substring(text.length() - maskEnd);
        }
    }
}
