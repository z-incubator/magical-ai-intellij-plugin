
package com.ai.action.editor;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.ai.gui.text.TextEditorHelper.selectedTextEditors;

public class SelectedTextEditorTargetedAction extends ActionGroup {

    public static final Predicate<TextEditor> ALL = __ -> true;

    public static final Predicate<TextEditor> ALL_WITH_SELECTION = e -> e.getEditor().getSelectionModel().hasSelection();

    public static final Predicate<TextEditor> WRITABLE = e -> e.getEditor().getDocument().isWritable();

    private final Predicate<TextEditor> supportedEditors;
    private final Function<TextEditor, AnAction> actionFactory;
    private final boolean alwaysVisible;


    public SelectedTextEditorTargetedAction(
            String text,
            String description,
            Icon icon,
            Predicate<TextEditor> supportedEditors,
            Function<TextEditor, AnAction> actionFactory,
            boolean alwaysVisible) {
        super(text, description, icon);
        this.supportedEditors = supportedEditors;
        this.actionFactory = actionFactory;
        this.alwaysVisible = alwaysVisible;
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }

    protected void disableOrHideAction(Presentation presentation) {
        presentation.setPopupGroup(false);
        if (alwaysVisible)
            presentation.setEnabled(false);
        else
            presentation.setVisible(false);
    }

    protected void enableAction(Presentation presentation) {
        presentation.setEnabled(true);
        presentation.setPopupGroup(false);
    }

    protected void enableActionGroup(Presentation presentation) {
        presentation.setEnabled(true);
        presentation.setPopupGroup(true);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        long selectedTextEditorCount = selectedTextEditors(e.getProject(), supportedEditors).count();

        if (selectedTextEditorCount == 0) {
            disableOrHideAction(e.getPresentation());
        } else if (selectedTextEditorCount > 1) {
            enableActionGroup(e.getPresentation());
        } else {
            enableAction(e.getPresentation());
        }
    }

    private static final AnAction[] ZERO_CHILDREN = new AnAction[0];

    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
        Project project;
        if (e == null || (project = e.getProject()) == null)
            return ZERO_CHILDREN;

        AnAction[] children = selectedTextEditors(project, supportedEditors)
                .map(textEditor -> new TextEditorActionProxy(textEditor, actionFactory.apply(textEditor)))
                .toArray(AnAction[]::new);
        if (children.length == 1 && children[0] instanceof TextEditorActionProxy proxyAction)
            children[0] = proxyAction.getTargetAction();

        return children;
    }
}
