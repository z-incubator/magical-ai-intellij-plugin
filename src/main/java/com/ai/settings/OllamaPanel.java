
package com.ai.settings;

import com.ai.i18n.Bundle;
import com.ai.chat.models.ModelFamily;
import com.ai.chat.models.ModelType;
import com.intellij.openapi.options.Configurable;

import java.util.function.Predicate;

import static com.ai.chat.AssistantType.System.Ollama;

public class OllamaPanel extends BaseModelPanel implements Configurable {
    private static final Predicate<ModelType> ollamaModels = model -> model.getFamily() == ModelFamily.OLLAMA;

    public OllamaPanel() {
        super(Ollama, ollamaModels);
    }

    @Override
    public String getDisplayName() {
        return Bundle.get("ui.setting.menu.text");
    }

    @Override
    protected boolean isModelNameEditable() {
        return true;
    }
}
