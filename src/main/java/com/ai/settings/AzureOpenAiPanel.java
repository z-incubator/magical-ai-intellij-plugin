
package com.ai.settings;

import com.ai.i18n.Bundle;
import com.ai.chat.models.ModelType;
import com.intellij.openapi.options.Configurable;

import java.util.function.Predicate;

import static com.ai.chat.AssistantType.System.*;

public class AzureOpenAiPanel extends BaseModelPanel implements Configurable {
    private static final Predicate<ModelType> anyModel = __ -> true;

    public AzureOpenAiPanel() {
        super(AzureOpenAI, anyModel);
    }

    @Override
    public final boolean isAzureCompatible() {
        return true;
    }

    @Override
    public String getDisplayName() {
        return Bundle.get("ui.setting.menu.text");
    }

}
