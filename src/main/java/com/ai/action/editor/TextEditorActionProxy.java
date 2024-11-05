
package com.ai.action.editor;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.TextEditor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public class TextEditorActionProxy extends AnAction {

    private final AnAction targetAction;


    public TextEditorActionProxy(TextEditor textEditor, AnAction targetAction) {
        super(textEditor.getFile().getName());
        this.targetAction = targetAction;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        targetAction.actionPerformed(anActionEvent);
    }
}
