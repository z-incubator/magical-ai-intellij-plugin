
package com.ai.settings;

import com.ai.i18n.Bundle;
import com.ai.chat.models.ModelFamily;
import com.ai.chat.models.ModelType;
import com.intellij.openapi.options.Configurable;

import java.util.function.Predicate;

import static com.ai.chat.AssistantType.System.Claude;

public class ClaudePanelBase extends BaseModelPanel implements Configurable {
    private static final Predicate<ModelType> claudeModels = model -> model.getFamily() == ModelFamily.ANTHROPIC;

    public ClaudePanelBase() {
        super(Claude, claudeModels);
    }

    @Override
    public String getDisplayName() {
        return Bundle.get("ui.setting.menu.text");
    }
}
