package com.ai.gui.tool.window;

import com.intellij.icons.AllIcons;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.panels.VerticalLayout;
import com.intellij.util.ui.JBUI;
import com.ai.i18n.Bundle;
import com.ai.chat.models.ModelType;
import com.ai.settings.state.GeneralSettings;
import com.ai.text.CodeSnippetManipulator;
import com.ai.text.TextFragment;
import com.ai.util.ThemeTool;
import com.ai.icons.AIIcons;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;

import javax.accessibility.AccessibleContext;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.atomic.AtomicReference;

public class ConversationTurnPanel extends JBPanel<ConversationTurnPanel> {

    private static final Logger LOG = Logger.getInstance(ConversationTurnPanel.class);

    private final MessagePanel messagePanel;

    private volatile Message message;


    public ConversationTurnPanel(Message message, ModelType model) {
        this.message = message;
        var fromUser = (model == null);
        setDoubleBuffered(true);
        setOpaque(true);
        setBackground(fromUser ? new JBColor(0xF7F7F7, 0x3C3F41) : new JBColor(0xEBEBEB, 0x2d2f30));
        setBorder(JBUI.Borders.empty(JBUI.scale(4), JBUI.scale(1)));
        setLayout(new BorderLayout(JBUI.scale(2), 0));

        if (GeneralSettings.getInstance().isEnableAvatar()) {
            JPanel iconPanel = new JPanel(new BorderLayout());
            iconPanel.setBorder(JBUI.Borders.empty(JBUI.scale(7), JBUI.scale(7), JBUI.scale(7), 0));
            iconPanel.setOpaque(false);
            Icon imageIcon;
            if (fromUser) {
                imageIcon = ThemeTool.isDark() ? AIIcons.USER_DARK : AIIcons.USER_LIGHT;
            } else {
                imageIcon = ThemeTool.isDark() ? AIIcons.AI_MODEL_DARK : AIIcons.AI_MODEL_LIGHT;
            }
            iconPanel.add(new JBLabel(imageIcon), BorderLayout.NORTH);
            add(iconPanel, BorderLayout.WEST);
        }
        JPanel centerPanel = new JPanel(new VerticalLayout(JBUI.scale(0)));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(JBUI.Borders.emptyLeft(JBUI.scale(5)));
        centerPanel.add(messagePanel = createMessagePanel(message, fromUser));
        add(centerPanel, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new BorderLayout());
        actionPanel.setOpaque(false);
        actionPanel.setBorder(JBUI.Borders.empty(JBUI.scale(7), 0, 0, JBUI.scale(10)));
        JLabel copyAction = new JLabel(AllIcons.Actions.Copy);
        copyAction.setCursor(new Cursor(Cursor.HAND_CURSOR));
        copyAction.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Transferable transferable = new StringSelection(getMessageText().markdown());
                CopyPasteManager.getInstance().setContents(transferable);
                Notifications.Bus.notify(
                        new Notification(Bundle.get("group.id"),
                                "Copied successfully",
                                "ChatGPT " + (fromUser ? "prompt" : "reply") + " content has been successfully copied to the clipboard.",
                                NotificationType.INFORMATION));
            }
        });
        actionPanel.add(copyAction, BorderLayout.NORTH);
        add(actionPanel, BorderLayout.EAST);
    }

    public TextFragment getMessageText() {
        return TextFragment.of(message.getContent());
    }

    public String toDisplayText(TextFragment text, boolean fromUser) {
        if (!fromUser)
            return text.toHtml();

        String markdown = text.markdown();
        StringBuilder buf = new StringBuilder(markdown.length());
        char ch;
        boolean onLineStart = true;
        for (int i = 0; i < markdown.length(); i++) {
            switch (ch = markdown.charAt(i)) {
                case '\r' -> {
                }
                case '\n' -> {
                    onLineStart = true;
                    buf.append("<br>");
                }
                case '<' -> {
                    onLineStart = false;
                    buf.append("&lt;");
                }
                case '>' -> {
                    onLineStart = false;
                    buf.append("&gt;");
                }
                case '&' -> {
                    onLineStart = false;
                    buf.append("&amp;");
                }
                default -> {
                    if (onLineStart && Character.isWhitespace(ch)) {
                        buf.append("&nbsp;");
                        if (ch == '\t') buf.append("&nbsp;&nbsp;&nbsp;");
                    } else {
                        onLineStart = false;
                        buf.append(ch);
                    }
                }
            }
        }
        return buf.toString();
    }

    public MessagePanel createMessagePanel(Message message, boolean fromUser) {
        TextFragment content = TextFragment.of(message.getContent());

        MessageTextPanel messagePanel = new MessageTextPanel(message.getMessageType() == MessageType.USER);
        messagePanel.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        messagePanel.setContentType("text/html; charset=UTF-8");
        messagePanel.setOpaque(false);
        messagePanel.setBorder(null);

        if (fromUser && !StringUtils.startsWithAny(message.getContent(), "[" + Bundle.get("code.fragment.title"), "[Selected code")) {
            messagePanel.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0)); // 添加顶部填充
        }
        messagePanel.putClientProperty(AccessibleContext.ACCESSIBLE_NAME_PROPERTY, getMessageText().markdown());
        messagePanel.updateMessage(fromUser ? TextFragment.of(content.markdown(), CodeSnippetManipulator.makeCodeSnippetBlocksCollapsible(toDisplayText(content, true))) : content);
        messagePanel.setEditable(false);
        if (messagePanel.getCaret() != null) {
            messagePanel.setCaretPosition(0);
        }

        messagePanel.revalidate();
        messagePanel.repaint();

        return new MessagePanel(message, messagePanel);
    }

    private final AtomicReference<TextFragment> pendingTextContent = new AtomicReference<>();
    private final Timer updateContentTimer = new Timer(20, this::updateContentIncrementally);

    public void setContent(AssistantMessage message, TextFragment textContent) {
        this.message = message;
        this.pendingTextContent.set(textContent);
        if (!updateContentTimer.isRunning()) {
            updateContentTimer.setRepeats(false);
            updateContentTimer.start();
        }
    }

    public void setErrorContent(String errorMessage) {
        setContent(new AssistantMessage(errorMessage), TextFragment.of(errorMessage));
    }

    protected void updateContentIncrementally(ActionEvent event) {
        TextFragment pending = null;
        try {
            pending = pendingTextContent.get();
            if (pending != null) {
                messagePanel.updateTextContent(pending);
                pendingTextContent.compareAndSet(pending, null);
            }
        } catch (Exception e) {
            LOG.error("ChatGPT Exception in processing response: response: {}, error: {}", e, String.valueOf(pending), e.getMessage());
        }
    }
}
