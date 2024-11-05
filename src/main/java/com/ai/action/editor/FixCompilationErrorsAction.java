package com.ai.action.editor;

import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerEx;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.util.concurrency.annotations.RequiresReadLock;
import com.ai.action.ModelValidationUtils;
import com.ai.chat.ChatLink;
import com.ai.icons.AIIcons;
import com.ai.text.CodeFragmentFactory;
import com.ai.i18n.Bundle;
import com.ai.util.ThemeTool;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class FixCompilationErrorsAction extends AbstractEditorAction {

    private static final String DEFAULT_PROMPT = """
            Please identify and fix the compilation errors or warnings in the highlighted code snippet.
             Return the corrected code without highlights or provide guidance if needed.
            """;

    public FixCompilationErrorsAction() {
        super(Bundle.get("action.code.fix.compilation.error"), "Use the latest syntax and specification fixes", ThemeTool.isDark() ? AIIcons.QIANFAN_DARK : AIIcons.QIANFAN_LIGHT);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(true);
    }

    @Override
    protected void actionPerformed(Project project, Editor editor, String selectedText) {
        ApplicationManager.getApplication().invokeLater(() -> analyseHighlights(project, editor));
    }

    @RequiresReadLock
    protected void analyseHighlights(Project project, Editor editor) {
        if (ModelValidationUtils.isModelValid(project)) {
            return;
        }
        var severity = "error";
        var highlights = findCompilationInfos(project, editor, HighlightSeverity.ERROR);

        if (highlights.isEmpty()) {
            highlights = findCompilationInfos(project, editor, HighlightSeverity.WARNING);
            severity = "warning";
        }

        StringBuilder buf = new StringBuilder(editor.getDocument().getText());
        highlights.sort(Comparator.comparing(HighlightInfo::getEndOffset));

        addHighlightTagsToText(buf, highlights);

        ChatLink.forProject(project)
                .pushMessage(getPrompt(severity, highlights.size()),
                        List.of(CodeFragmentFactory.create(editor, buf.toString())));
    }

    private void addHighlightTagsToText(StringBuilder text, List<HighlightInfo> highlights) {
        Map<Integer, String> tags = new TreeMap<>();

        for (HighlightInfo highlight : highlights) {
            int endOffset = highlight.getEndOffset();
            int startOffset = highlight.getStartOffset();

            tags.put(endOffset, "</Highlight>");
            tags.put(startOffset, "<Highlight severity=\"" + highlight.getSeverity().getDisplayName()
                    + "\" description=\"" + highlight.getDescription() + "\">");
        }

        AtomicInteger delta = new AtomicInteger();
        tags.forEach((key, value) -> text.insert(key + delta.getAndAdd(value.length()), value));
    }

    private @NotNull List<HighlightInfo> findCompilationInfos(Project project, Editor editor, HighlightSeverity severity) {
        List<HighlightInfo> highlights = new ArrayList<>();
        DaemonCodeAnalyzerEx.processHighlights(
                editor.getDocument(), project, severity, 0, editor.getDocument().getTextLength(),
                highlights::add);
        return highlights;
    }

    private static String getPrompt(String severity, int numHighlights) {
        return DEFAULT_PROMPT.replace("{error|warning}", severity)
                .replace("(s)", numHighlights == 1 ? "" : "s");
    }
}
