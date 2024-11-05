
package com.ai.gui;

import com.ai.text.CodeFragment;
import com.ai.text.CodeFragmentFactory;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;

import java.util.ArrayList;
import java.util.List;

public class TextEditorSelectionAwareAppetizer implements ContextAwareAppetizer {

    @Override
    public List<CodeFragment> fetchSnippets(Project project) {
        List<CodeFragment> selectedFragments = new ArrayList<>();

        for (FileEditor editor : FileEditorManager.getInstance(project).getSelectedEditors()) {
            if (editor instanceof TextEditor textEditor) {
                CodeFragmentFactory.createFromSelection(textEditor.getEditor()).ifPresent(selectedFragments::add);
            }
        }
        return selectedFragments;
    }
}
