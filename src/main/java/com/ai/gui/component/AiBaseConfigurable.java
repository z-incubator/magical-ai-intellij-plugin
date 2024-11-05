package com.ai.gui.component;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.util.NlsContexts;
import com.ai.gui.AiSettingPanel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * 配置主类
 */
public class AiBaseConfigurable implements Configurable {

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "Magical AI";
    }

    @Override
    public @Nullable
    JComponent createComponent() {
        return new AiSettingPanel();
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() {
    }
}
