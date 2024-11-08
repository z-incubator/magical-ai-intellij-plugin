
package com.ai.action.editor;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsActions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.function.Supplier;

public abstract class AbstractEditorAction extends AnAction {

    protected String text = "";
    protected String key = "";

    protected AbstractEditorAction(
            @Nullable @NlsActions.ActionText String text,
            @Nullable @NlsActions.ActionDescription String description,
            @Nullable Icon icon) {
        super(text, description, icon);
    }

    protected AbstractEditorAction(
            @NotNull Supplier<@NlsActions.ActionText String> dynamicText,
            @NotNull Supplier<@NlsActions.ActionText String> dynamicDescription) {
        super(dynamicText, dynamicDescription, (Icon)null);
    }

    protected AbstractEditorAction(
            @NotNull Supplier<@NlsActions.ActionText String> dynamicText,
            @NotNull Supplier<@NlsActions.ActionText String> dynamicDescription,Icon icon) {
        super(dynamicText, dynamicDescription, icon);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        var project = event.getProject();
        var editor = event.getData(PlatformDataKeys.EDITOR);
        if (editor != null && project != null) {
            actionPerformed(project, editor, editor.getSelectionModel().getSelectedText());
        }
    }

    protected abstract void actionPerformed(Project project, Editor editor, String selectedText);

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        var editor = e.getData(CommonDataKeys.EDITOR);
        var hasSelection = editor != null && editor.getSelectionModel().hasSelection();
        e.getPresentation().setEnabledAndVisible(hasSelection);
    }
}
