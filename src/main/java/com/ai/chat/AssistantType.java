
package com.ai.chat;

import com.ai.settings.*;
import com.intellij.openapi.options.Configurable;
import com.ai.chat.models.ModelFamily;
import com.ai.settings.state.GeneralSettings;
import lombok.Getter;

public sealed interface AssistantType
        permits AssistantType.System, AssistantType.Custom {

    String name();

    ModelFamily getFamily();

    Class<? extends Configurable> getConfigurable();

    @Getter
    enum System implements AssistantType {
        OpenAI(ModelFamily.OPEN_AI, OpenAIPanel.class),
        AzureOpenAI(ModelFamily.AZURE_OPENAI, AzureOpenAiPanel.class),
        Claude(ModelFamily.ANTHROPIC, ClaudePanelBase.class),
        Ollama(ModelFamily.OLLAMA, OllamaPanel.class),
        Qianfan(ModelFamily.QIANFAN, QianfanPanel.class),
        OnlineTool(null, GeneralSettingsPanel.class);

        private final ModelFamily family;
        private final Class<? extends Configurable> configurable;

        System(ModelFamily family, Class<? extends Configurable> configurable) {
            this.family = family;
            this.configurable = configurable;
        }

        public boolean isEnabled(GeneralSettings settings) {
            return settings.selectedModel().contains(this);
        }
    }

    record Custom(
            String name,
            String displayName,
            ModelFamily getFamily,
            Class<? extends Configurable> getConfigurable
    ) implements AssistantType {
    }

}
