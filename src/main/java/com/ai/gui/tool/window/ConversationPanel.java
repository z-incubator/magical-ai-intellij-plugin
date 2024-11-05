package com.ai.gui.tool.window;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.NullableComponent;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.labels.LinkLabel;
import com.intellij.ui.components.panels.VerticalLayout;
import com.intellij.util.ui.JBFont;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.ai.core.SystemMessageHolder;
import com.ai.chat.ChatLink;
import com.ai.chat.metadata.ImmutableUsage;
import com.ai.chat.models.ModelType;
import com.ai.gui.component.AiBaseComponent;
import com.ai.event.ListenerList;
import com.ai.settings.state.GeneralSettings;
import com.ai.util.ScrollingTools;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.Usage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

public class ConversationPanel extends JBPanel<ConversationPanel> implements NullableComponent, SystemMessageHolder {
    private final JPanel basePanel = new JPanel(new VerticalLayout(0));
    private final JBScrollPane myScrollPane = new JBScrollPane(basePanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    private int myScrollValue = 0;
    private final UsagePanel usagePanel;
    private final ChatLink chatLink;
    private final ListenerList<Runnable> onChatMemoryCleared = ListenerList.of(Runnable.class);

    public ConversationPanel(ChatLink chatLink, @NotNull Project project) {
        this.chatLink = chatLink;
        setBorder(JBUI.Borders.empty());
        setLayout(new BorderLayout());
        setBackground(UIUtil.getListBackground());

        myScrollPane.getVerticalScrollBar().putClientProperty(JBScrollPane.IGNORE_SCROLLBAR_IN_INSETS, Boolean.TRUE);
        ScrollingTools.installAutoScrollToBottom(myScrollPane);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(JBUI.Borders.emptyLeft(0));


        add(mainPanel, BorderLayout.CENTER);

        JBLabel myTitle = new JBLabel(GeneralSettings.getInstance().defaultModel);
        myTitle.setForeground(JBColor.namedColor("Label.infoForeground", new JBColor(Gray.x80, Gray.x8C)));
        myTitle.setFont(JBFont.label());

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(myTitle, BorderLayout.WEST);

        this.usagePanel = new UsagePanel();

        LinkLabel<String> newChat = new LinkLabel<>("New chat", null);
        newChat.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                basePanel.removeAll();
                myTitle.setText(GeneralSettings.getInstance().defaultModel);
                addAssistantTipsIfEnabled(false);
                basePanel.updateUI();
                chatLink.getConversationContext().clear();
                onChatMemoryCleared.fire().run();
                usagePanel.updateUsage(ImmutableUsage.empty(), chatLink.getConversationContext().getModelType());
            }
        });

        newChat.setFont(JBFont.label());
        newChat.setBorder(JBUI.Borders.emptyRight(20));
        panel.add(newChat, BorderLayout.EAST);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 0)); // 添加顶部填充

        //  panel.add(usagePanel, BorderLayout.WEST);
        mainPanel.add(panel, BorderLayout.NORTH);

        basePanel.setOpaque(true);
        basePanel.setBackground(UIUtil.getListBackground());
        basePanel.setBorder(JBUI.Borders.emptyRight(0));

        myScrollPane.setBorder(JBUI.Borders.empty());
        mainPanel.add(myScrollPane);
        myScrollPane.getVerticalScrollBar().setAutoscrolls(true);
        myScrollPane.getVerticalScrollBar().addAdjustmentListener(e -> {
            myScrollValue = e.getValue();
        });

        addAssistantTipsIfEnabled(true);
    }

    public void addSeparator(JComponent comp) {
        SwingUtilities.invokeLater(() -> {
            JSeparator separator = new JSeparator();
            separator.setForeground(JBColor.border());
            comp.add(separator);
            updateLayout();
            invalidate();
            validate();
            repaint();
        });
    }

    protected void addAssistantTipsIfEnabled(boolean firstUse) {
        addSeparator(basePanel);

        var introEnabled = GeneralSettings.getInstance().getEnableInitialMessage();
        if (!firstUse && introEnabled == null)
            GeneralSettings.getInstance().setEnableInitialMessage(introEnabled = false);
        if (!Boolean.FALSE.equals(introEnabled))
            basePanel.add(createAssistantTips());
    }

    protected ConversationTurnPanel createAssistantTips() {
        var modelType = chatLink.getConversationContext().getModelType();
        var tips = """
                How can I help you?
                                
                Here are some suggestions to get you started:
                                
                [🖊️ What can AI do? ](assistant://?prompt=What+can+AI+do?) \s
                [🖊️ Write a beautiful article ](assistant://?prompt=Write+a+beautiful+article?)
                [🖊️ Write a piece of code ](assistant://?prompt=Write+a+piece+of+code?)
                """;

        var cnTips = """
                有什么可以帮忙的？
                                
                这里有一些建议供您参考：
                               
                [🖊️ AI 能做什么 ](assistant://?prompt=AI+能做什么) \s
                [🖊️ 写一篇优美文章 ](assistant://?prompt=写一篇优美文章) \s
                [🖊️ 写一段代码 ](assistant://?prompt=写一段代码)
                  """;
        return new ConversationTurnPanel(new AssistantMessage(Objects.requireNonNull(AiBaseComponent.getInstance().getState()).getLocale().equals("English") ? tips : cnTips), modelType);
    }

    public void add(ConversationTurnPanel conversationTurnPanel) {
        SwingUtilities.invokeLater(() -> {
            basePanel.add(conversationTurnPanel);
            updateLayout();
            scrollToBottom();
            invalidate();
            validate();
            repaint();
        });
    }

    public void onChatMemoryCleared(Runnable action) {
        onChatMemoryCleared.addListener(action);
    }

    public void scrollToBottom() {
        ScrollingTools.scrollToBottom(myScrollPane);
    }

    public void updateLayout() {
        LayoutManager layout = basePanel.getLayout();
        int componentCount = basePanel.getComponentCount();
        for (int i = 0; i < componentCount; i++) {
            layout.removeLayoutComponent(basePanel.getComponent(i));
            layout.addLayoutComponent(null, basePanel.getComponent(i));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (myScrollValue > 0) {
            g.setColor(JBColor.border());
            int y = myScrollPane.getY() - 1;
            g.drawLine(0, y, getWidth(), y);
        }
    }

    @Override
    public boolean isVisible() {
        if (super.isVisible()) {
            int count = basePanel.getComponentCount();
            for (int i = 0; i < count; i++) {
                if (basePanel.getComponent(i).isVisible()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isNull() {
        return !isVisible();
    }

    @Override
    public String getSystemMessage() {
        return "";
    }

    public void updateUsage(Usage usage, ModelType model) {
        usagePanel.updateUsage(usage, model);
    }
}
