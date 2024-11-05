
package com.ai.settings;

import com.ai.i18n.Bundle;
import com.ai.chat.models.ModelFamily;
import com.ai.chat.models.ModelType;
import com.intellij.openapi.options.Configurable;

import java.util.function.Predicate;

import static com.ai.chat.AssistantType.System.OpenAI;

public class OpenAIPanel extends BaseModelPanel implements Configurable {
    private static final Predicate<ModelType> openAiModels = model -> model.getFamily() == ModelFamily.OPEN_AI;

    public OpenAIPanel() {
        super(OpenAI, openAiModels);
    }

    @Override
    public String getDisplayName() {
        return Bundle.get("ui.setting.menu.text");
    }

    @Override
    protected boolean isModelNameEditable() {
        return true;
    }

    @Override
    protected boolean isStreamOptionsApiAvailable() {
        return true;
    }
}
