
package com.ai.action.editor;

import com.ai.i18n.Bundle;
import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffDialogHints;
import com.intellij.diff.DiffManager;
import com.intellij.diff.contents.DiffContent;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.diff.tools.util.base.IgnorePolicy;
import com.intellij.diff.tools.util.base.TextDiffSettingsHolder.TextDiffSettings;
import com.intellij.diff.util.DiffUserDataKeys;
import com.intellij.diff.util.DiffUtil;
import com.intellij.diff.util.Side;
import com.intellij.icons.AllIcons.Actions;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static com.ai.gui.text.TextEditorHelper.*;

public class DiffAction extends AnAction {

    private final TextEditor targetEditor;


    public DiffAction() {
        this((TextEditor)null);
    }

    public DiffAction(TextEditor targetEditor) {
        this(Bundle.get("editor.diff.action.name"), Bundle.get("editor.diff.action.desc"), Actions.Diff, targetEditor);
    }

    public DiffAction(String text, String description, Icon icon) {
        this(text, description, icon, null);
    }

    public DiffAction(String text, String description, Icon icon, TextEditor targetEditor) {
        super(text, description, icon);
        this.targetEditor = targetEditor;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        var project = event.getProject();
        if (project == null) {
            return;
        }

        var rightEditor = getTextArea(event);
        if (rightEditor == null) {
            return;
        }

        if (targetEditor != null) {
            showDiff(project, targetEditor, rightEditor);
        } else {
            selectedTextEditors(project, SelectedTextEditorTargetedAction.WRITABLE)
                    .forEach(leftEditor -> showDiff(project, leftEditor, rightEditor));
        }
    }

    protected void showDiff(Project project, TextEditor leftEditor, JTextArea rightEditor) {
        VirtualFile leftEditorFile = getEditorFile(leftEditor);
        DiffContentFactory diffContentFactory = DiffContentFactory.getInstance();
        TextRange leftTextRange = getDiffTargetTextRange(leftEditor);
        DiffContent leftContent = leftTextRange.isEmpty()
                ? diffContentFactory.create(project, leftEditorFile)
                : diffContentFactory.createFragment(project, leftEditor.getEditor().getDocument(), leftTextRange);

        SimpleDiffRequest request = new SimpleDiffRequest(
                Bundle.get("editor.diff.title"),
                leftContent,
                diffContentFactory.create(project, getSelectedTextOrEntireContent(rightEditor), leftEditorFile.getFileType()),
                leftEditorFile.getName(),
                Bundle.get("editor.diff.local.content.title")
        );
        TextDiffSettings settings = new TextDiffSettings();
        settings.setIgnorePolicy(IgnorePolicy.TRIM_WHITESPACES);
        request.putUserData(TextDiffSettings.KEY, settings);
        request.putUserData(DiffUserDataKeys.SCROLL_TO_LINE, Pair.create(Side.LEFT, DiffUtil.getCaretPosition(leftEditor.getEditor()).line));

        DiffManager.getInstance().showDiff(project, request, DiffDialogHints.DEFAULT);
    }

    public TextRange getDiffTargetTextRange(TextEditor editor) {
        return TextRange.EMPTY_RANGE;
    }

    public static class WithSelection extends DiffAction {

        public WithSelection(TextEditor textEditor) {
            super(Bundle.get("editor.diff.w/sel.action.name"), Bundle.get("editor.diff.w/sel.action.desc"), Actions.Diff, textEditor);
        }

        @Override
        public TextRange getDiffTargetTextRange(TextEditor editor) {
            SelectionModel selectionModel = editor.getEditor().getSelectionModel();
            return selectionModel.hasSelection()
                    ? new TextRange(selectionModel.getSelectionStart(), selectionModel.getSelectionEnd())
                    : TextRange.EMPTY_RANGE;
        }
    }
}
