package com.ai.gui.prompt.context;

import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public interface ListItemDescriptor<T> {

    @Nullable String getTextFor(T value);

    @Nullable String getTooltipFor(T value);

    @Nullable Icon getIconFor(T value);

    default Icon getSelectedIconFor(T value) {
        return getIconFor(value);
    }

    boolean hasSeparatorAboveOf(T value);

    @Nullable String getCaptionAboveOf(T value);
}
