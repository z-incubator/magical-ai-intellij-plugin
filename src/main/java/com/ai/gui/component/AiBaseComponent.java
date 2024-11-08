package com.ai.gui.component;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "AiBaseConfig", storages = {@Storage("AiBaseConfig.xml")})
public class AiBaseComponent implements PersistentStateComponent<AiBaseComponent.State> {
    private State state = new State();

    public static AiBaseComponent getInstance() {
        return ApplicationManager.getApplication().getService(AiBaseComponent.class);
    }


    @Override
    public @Nullable AiBaseComponent.State getState() {
        return this.state;
    }

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
    }


    @Data
    public static class State {
        private String locale = "English";
    }
}
