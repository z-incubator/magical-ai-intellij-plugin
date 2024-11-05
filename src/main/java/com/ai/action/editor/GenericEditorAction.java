
package com.ai.action.editor;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsActions;
import com.ai.chat.ChatLink;
import com.ai.action.ModelValidationUtils;
import com.ai.settings.state.GeneralSettings;
import com.ai.text.CodeFragmentFactory;
import com.ai.topic.OnlineTopic;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;
import java.util.function.Supplier;

public class GenericEditorAction extends AbstractEditorAction {

    private final String prompt;

    public GenericEditorAction(@NotNull Supplier<@NlsActions.ActionText String> dynamicText, String prompt) {
        super(dynamicText, () -> prompt);
        this.prompt = prompt;
    }

    public GenericEditorAction(@NotNull Supplier<@NlsActions.ActionText String> dynamicText, String prompt, Icon icon) {
        super(dynamicText, () -> prompt, icon);
        this.prompt = prompt;
    }

    public GenericEditorAction(String text, String prompt, Icon icon) {
        super(text, prompt, icon);
        this.prompt = prompt;
    }

    @Override
    protected void actionPerformed(Project project, Editor editor, String selectedText) {
        if (ModelValidationUtils.isModelValid(project)) {
            return;
        }

        if ("OnlineTool".equals(GeneralSettings.getInstance().defaultModel)) {
            project.getMessageBus().syncPublisher(OnlineTopic.TOPIC).send(selectedText);
            return;
        }
        ChatLink.forProject(project).pushMessage(prompt, List.of(CodeFragmentFactory.create(editor, selectedText)));
    }
}
