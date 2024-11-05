
package com.ai.settings;

import com.intellij.openapi.options.Configurable;
import com.ai.chat.models.ModelFamily;
import com.ai.chat.models.ModelType;

import java.util.function.Predicate;

import static com.ai.chat.AssistantType.System.Qianfan;

public class QianfanPanel extends BaseModelPanel implements Configurable {
    private static final Predicate<ModelType> qianfanModels = model -> model.getFamily() == ModelFamily.QIANFAN;

    public QianfanPanel() {
        super(Qianfan, qianfanModels);
    }

    @Override
    public String getDisplayName() {
        return "Qianfan";
    }

    @Override
    protected boolean isModelNameEditable() {
        return true;
    }

    @Override
    protected boolean isStreamOptionsApiAvailable() {
        return true;
    }

    @Override
    protected boolean isQianfan() {
        return true;
    }
}
