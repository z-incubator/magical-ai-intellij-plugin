
package com.ai.gui.view;

import com.intellij.icons.AllIcons;
import com.intellij.icons.AllIcons.Actions;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.ui.AnActionButton;
import com.intellij.util.ui.JBUI;
import com.ai.action.editor.DiffAction;
import com.ai.action.editor.SelectedTextEditorTargetedAction;
import com.ai.gui.text.TextEditorHelper;
import com.ai.gui.view.rsyntaxtextarea.RSyntaxTextAreaUIEx;
import com.ai.icons.AIIcons;
import com.ai.util.Language;
import com.ai.i18n.Bundle;
import com.ai.util.ThemeTool;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.ViewFactory;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseWheelListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.ai.gui.text.TextEditorHelper.TEXT_AREA_KEY;

public class RSyntaxTextAreaView extends ComponentView {

    private static final Logger log = Logger.getInstance(RSyntaxTextAreaView.class);
    private static Theme defaultTheme;

    private final Language language;

    public RSyntaxTextAreaView(Element element, Language language) {
        super(element);
        this.language = language;
    }

    @Override
    public void insertUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        super.insertUpdate(e, a, f);
        updateText();
    }

    @Override
    public void removeUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        super.removeUpdate(e, a, f);
        updateText();
    }

    @Override
    public void changedUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        super.changedUpdate(e, a, f);
        updateText();
    }

    protected void updateText() {
        Component comp = getComponent();
        if (comp instanceof RTextScrollPane scrollPane && scrollPane.getTextArea() instanceof RSyntaxTextArea textArea)
            updateText(scrollPane, textArea);
    }

    protected void updateText(RTextScrollPane scrollPane, RSyntaxTextArea textArea) {
        try {
            textArea.setText(getText());
        } catch (BadLocationException e) {
            log.warn("Unable to update RSyntaxTextArea text due to " + e, e);
        }
    }

    public String getText() throws BadLocationException {
        var element = getElement();
        var text = getDocument().getText(element.getStartOffset(), element.getEndOffset() - element.getStartOffset());
        if (text.endsWith("\n"))
            text = text.substring(0, text.length() + (text.endsWith("\r\n") ? -2 : -1));

        //text = Escaping.unescapeHtml(text);
        return text;
    }

    @Override
    protected Component createComponent() {
        try {
            return createComponent0();
        } catch (RuntimeException | Error e) {
            throw e;
        }
    }

    protected static class MyRSyntaxTextArea extends RSyntaxTextArea implements DataProvider {

        @Override
        public @Nullable Object getData(@NotNull @NonNls String dataId) {
            if (TEXT_AREA_KEY.is(dataId))
                return this;
            return null;
        }
    }

    protected Component createComponent0() {
        RSyntaxTextArea textArea = new MyRSyntaxTextArea();
        textArea.setUI(new RSyntaxTextAreaUIEx(textArea));
        textArea.setSyntaxEditingStyle(language.mimeType());
        textArea.setEditable(false);
        textArea.setCodeFoldingEnabled(true);
        textArea.setAnimateBracketMatching(false);
        textArea.setAntiAliasingEnabled(true);
        textArea.setLineWrap(true);
        textArea.setSize(4000, 4000);
        textArea.setMarkOccurrences(true);
        textArea.setMarkOccurrencesDelay(500);
        Theme theme = getDefaultTheme();
        if (theme != null)
            theme.apply(textArea);

        RTextScrollPane scrollPane = new RTextScrollPane(textArea) {
            @Override
            public Dimension getPreferredSize() {
                Container cont = RSyntaxTextAreaView.this.getContainer();
                if (cont != null && (getWidth() == 0 || getWidth() > cont.getWidth())) {
                    setSize(RSyntaxTextAreaView.this.getContainer().getWidth(), Integer.MAX_VALUE / 2);
                    doLayout();
                    getViewport().doLayout();
                }
                return super.getPreferredSize();
            }
        };
        scrollPane.setLineNumbersEnabled(false);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(6, 0, 5, 0));
        scrollPane.setWheelScrollingEnabled(false);
        scrollPane.setOpaque(false);
        for (MouseWheelListener listener : scrollPane.getMouseWheelListeners())
            scrollPane.removeMouseWheelListener(listener);

        JButton copyAction = new JButton(AllIcons.Actions.Copy);
        copyAction.setMargin(JBUI.emptyInsets());
        copyAction.setCursor(new Cursor(Cursor.HAND_CURSOR));
        copyAction.setFocusable(false);

        ActionGroup actionGroup = new ActionGroup() {
            @Override
            public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
                return new AnAction[]{new CopyCodeAction(AllIcons.Actions.Copy)};
            }
        };

        Presentation presentation = new Presentation();
        presentation.setIcon(Actions.More);
        presentation.putClientProperty(ActionButton.HIDE_DROPDOWN_ICON, Boolean.TRUE);
        ActionButton myCorner = new ActionButton(actionGroup, presentation, ActionPlaces.UNKNOWN, new Dimension(20, 20)) {
            @Override
            protected DataContext getDataContext() {
                return DataManager.getInstance().getDataContext(this);
            }
        };
        myCorner.setNoIconsInPopup(true);

        // 操作按钮
        JComponent corner = new CodeBlockActionPanel();
        textArea.add(corner);

        textArea.addComponentListener(new ComponentAdapter() {
            private static final int padding = 2;

            @Override
            public void componentResized(ComponentEvent e) {
                Component c = e.getComponent();
                if (corner.getHeight() <= 0) {
                    Dimension prefSize = corner.getPreferredSize();
                    corner.setSize(prefSize);
                }
                corner.setBounds(c.getWidth() - corner.getWidth() - padding, padding, corner.getWidth(), corner.getHeight());
            }
        });

        updateText(scrollPane, textArea);
        return scrollPane;
    }

    public static class CodeBlockActionPanel extends JPanel {

        public CodeBlockActionPanel() {
            super(new GridLayout(1, 0));
            setOpaque(false);
            createUI();
        }

        protected void createUI() {
            // Create a colored panel for buttons
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

            // Dynamically set the background color based on the theme
            Color backgroundColor = ThemeTool.isDark() ? new Color(50, 50, 50) : new Color(240, 240, 240);
            buttonPanel.setBackground(backgroundColor);

            Icon INSERT = ThemeTool.isDark() ? AIIcons.INSERT_DARK : AIIcons.INSERT_LIGHT;
            buttonPanel.add(createActionButton(new CopyCodeAction(AllIcons.Actions.Copy)));
            buttonPanel.add(createActionButton(new InsertCodeAction(INSERT)));
            buttonPanel.add(createMoreActionButton());

            add(buttonPanel);
        }

        protected ActionButton createActionButton(AnAction action) {
            Presentation presentation = new Presentation();
            presentation.setIcon(Actions.More);
            presentation.putClientProperty(ActionButton.HIDE_DROPDOWN_ICON, Boolean.TRUE);
            ActionButton actionButton = new ActionButton(action, action.getTemplatePresentation().clone(), ActionPlaces.UNKNOWN, new Dimension(20, 20)) {
                @Override
                protected DataContext getDataContext() {
                    return DataManager.getInstance().getDataContext(this);
                }
            };
            actionButton.setNoIconsInPopup(true);
            return actionButton;
        }

        protected ActionButton createMoreActionButton() {
            var moreActions = new DefaultActionGroup();
            moreActions.getTemplatePresentation().setPopupGroup(true);
            moreActions.getTemplatePresentation().setIcon(Actions.More);
            moreActions.getTemplatePresentation().putClientProperty(ActionButton.HIDE_DROPDOWN_ICON, true);
            moreActions.add(new SelectedTextEditorTargetedAction(
                    "Compare with Selection", "Compare with Selection",
                    Actions.Diff, SelectedTextEditorTargetedAction.ALL_WITH_SELECTION,
                    DiffAction.WithSelection::new, false));
            moreActions.add(new SelectedTextEditorTargetedAction(
                    "Compare with Editor", "Compare with Editor",
                    Actions.Diff, SelectedTextEditorTargetedAction.WRITABLE,
                    DiffAction::new, true));

            var actionButton = new ActionButton(moreActions, null, ActionPlaces.UNKNOWN, new Dimension(20, 20)) {
                @Override
                protected DataContext getDataContext() {
                    return DataManager.getInstance().getDataContext(this);
                }
            };
            actionButton.setNoIconsInPopup(true);
            return actionButton;
        }
    }

    private static abstract class RSyntaxTextAreaAction extends AnActionButton {
        protected RSyntaxTextAreaAction(String text, String description, Icon icon) {
            super(text, description, icon);
        }

        @Override
        public @NotNull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.EDT;
        }

        protected String getTextContent(JTextArea textArea) {
            return TextEditorHelper.getSelectedTextOrEntireContent(textArea);
        }
    }

    private static class CopyCodeAction extends RSyntaxTextAreaAction {

        CopyCodeAction(Icon icon) {
            super("Copy", "Copy to Clipboard", icon);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            JTextArea textArea = e.getData(TEXT_AREA_KEY);
            if (textArea != null) {
                Transferable transferable = new StringSelection(getTextContent(textArea));
                CopyPasteManager.getInstance().setContents(transferable);
            }
        }
    }

    private static class InsertCodeAction extends RSyntaxTextAreaAction {

        InsertCodeAction(Icon icon) {
            super("Insert Code Block at Cursor", "Insert Code Block at Cursor", icon);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            Project project = e.getProject();
            if (project == null)
                return;

            JTextArea textArea = e.getData(TEXT_AREA_KEY);
            if (textArea == null)
                return;

            String targetText = getTextContent(textArea);
            FileEditorManager fileEditorManager = FileEditorManager.getInstance(e.getProject());
            List<TextEditor> textEditors = Arrays.stream(fileEditorManager.getSelectedEditors())
                    .filter(editor -> editor instanceof TextEditor)
                    .map(editor -> (TextEditor) editor)
                    .filter(editor -> editor.getEditor().getDocument().isWritable())
                    .toList();

            if (textEditors.isEmpty())
                return;

            if (textEditors.size() > 1) {
                JBPopupFactory.getInstance().createActionGroupPopup(Bundle.get("popup.title.paste.target"),
                                new PasteTargetGroup(textEditors, targetText), e.getDataContext(),
                                JBPopupFactory.ActionSelectionAid.SPEEDSEARCH, false)
                        .showUnderneathOf(Objects.requireNonNull(e.getInputEvent()).getComponent());
                return;
            }

            insertCodeFragment(textEditors.get(0), targetText);
        }
    }

    public static class PasteTargetGroup extends ActionGroup {

        private final List<TextEditor> textEditors;
        private final String targetText;

        public PasteTargetGroup(List<TextEditor> textEditors, String targetText) {
            this.textEditors = textEditors;
            this.targetText = targetText;
        }

        @Override
        public AnAction @NotNull [] getChildren(AnActionEvent e) {
            return textEditors.stream()
                    .map(textEditor -> new PasteTargetAction(textEditor, targetText))
                    .toArray(AnAction[]::new);
        }

        private static class PasteTargetAction extends AnAction {

            private final TextEditor textEditor;
            private final String targetText;

            PasteTargetAction(TextEditor textEditor, String targetText) {
                super(textEditor.getFile().getName());
                this.textEditor = textEditor;
                this.targetText = targetText;
            }

            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                insertCodeFragment(textEditor, targetText);
            }
        }
    }

    private static void insertCodeFragment(TextEditor textEditor, String targetText) {
        Editor currentEditor = textEditor.getEditor();
        Project project = textEditor.getEditor().getProject();
        SelectionModel selectionModel = currentEditor.getSelectionModel();
        String selectedText = selectionModel.getSelectedText();

        var document = currentEditor.getDocument();
        assert project != null;
        var codeStyleManager = CodeStyleManager.getInstance(project);

        // If selected text is not empty, replace selected text in the editor with the target text
        if (selectedText != null && !selectedText.isEmpty()) {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                int startOffset = selectionModel.getSelectionStart();
                int endOffset = selectionModel.getSelectionEnd();
                document.replaceString(startOffset, endOffset, targetText);
                // Adjust the indentation of the inserted text
                PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
                var psiFile = psiDocumentManager.getPsiFile(document);
                if (psiFile != null) {
                    psiDocumentManager.commitDocument(document);
                    codeStyleManager.adjustLineIndent(psiFile, new TextRange(startOffset, startOffset + targetText.length()));
                }
            });
        } else { // Otherwise, insert target text at the current cursor position
            int caretOffset = currentEditor.getCaretModel().getOffset();
            WriteCommandAction.runWriteCommandAction(project, () -> {
                document.insertString(caretOffset, targetText);
                // Update the selection after inserting the text
                selectionModel.setSelection(caretOffset, caretOffset + targetText.length());
                // Adjust the indentation of the inserted text
                PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
                var psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
                if (psiFile != null) {
                    psiDocumentManager.commitDocument(document);
                    codeStyleManager.adjustLineIndent(psiFile, new TextRange(caretOffset, caretOffset + targetText.length()));
                }
            });
        }
    }

    private Theme getDefaultTheme() {
        if (defaultTheme == null) {
            try {
                defaultTheme = Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/dark.xml"));
            } catch (IOException e) {
                log.warn("Unable to load RSyntaxTextArea theme due to " + e, e);
            }
        }
        return defaultTheme;
    }
}
