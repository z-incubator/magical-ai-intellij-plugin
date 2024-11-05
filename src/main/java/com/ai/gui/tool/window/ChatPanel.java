package com.ai.gui.tool.window;

import com.ai.chat.*;
import com.intellij.icons.AllIcons;
import com.intellij.ide.ui.laf.darcula.ui.DarculaButtonUI;
import com.intellij.notification.BrowseNotificationAction;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.components.JBTextArea;
import com.ai.core.Errors;
import com.ai.action.SettingAction;
import com.ai.chat.models.ModelFamily;
import com.ai.chat.models.ModelType;
import com.ai.core.ChatCompletionParser;
import com.ai.gui.ContextAwareAppetizer;
import com.ai.gui.MainConversationHandler;
import com.ai.gui.RoundedPanel;
import com.ai.gui.listener.SubmitListener;
import com.ai.icons.AIIcons;
import com.ai.settings.state.GeneralSettings;
import com.ai.topic.FileTopic;
import com.ai.i18n.Bundle;
import com.ai.util.ThemeTool;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Subscription;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.Generation;
import reactor.core.Disposable;

import javax.swing.*;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

import static java.awt.event.InputEvent.CTRL_DOWN_MASK;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class ChatPanel implements ChatMessageListener, ChatLinkProvider {

    private final JBTextArea userMessageTextField;
    private final JButton submitButton;
    private final JButton stopGenerating;
    private final @Getter ConversationPanel contentPanel;
    private final OnePixelSplitter splitter;
    @Getter
    private final Project project;
    private final JPanel actionPanel;
    @Setter
    private volatile Object requestHolder;
    private final ChatLink chatLink;

    public static final KeyStroke SUBMIT_KEYSTROKE = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, CTRL_DOWN_MASK);
    private final JPanel filePanel;

    public ChatPanel(@NotNull Project project, AssistantConfiguration configuration) {
        this.project = project;
        MainConversationHandler conversationHandler = new MainConversationHandler(this);
        chatLink = new ChatLinkService(project, conversationHandler, configuration.withSystemPrompt(() -> getContentPanel().getSystemMessage()));
        chatLink.addChatMessageListener(this);
        ContextAwareAppetizer contextAwareAppetizer = ApplicationManager.getApplication().getService(ContextAwareAppetizer.class);
        SubmitListener submitAction = new SubmitListener(chatLink, this::getSearchText, this::setSearchText, contextAwareAppetizer);

        splitter = new OnePixelSplitter(true, .98f);
        splitter.setDividerWidth(1);
        splitter.putClientProperty(HyperlinkListener.class, submitAction);

        userMessageTextField = createPromptTextArea(submitAction);
        userMessageTextField.registerKeyboardAction(submitAction, SUBMIT_KEYSTROKE, JComponent.WHEN_FOCUSED);
        userMessageTextField.getEmptyText().setText("Ask Magical AI");

        submitButton = createSendButton(submitAction);
        submitButton.setUI(new DarculaButtonUI());

        stopGenerating = createStopButton();

        filePanel = new JPanel();
        filePanel.setVisible(false);

        actionPanel = createActionPanel();
        contentPanel = new ConversationPanel(chatLink, project);

        // 添加引用文件
        contentPanel.add(filePanel, BorderLayout.SOUTH);
        project.getMessageBus().connect().subscribe(FileTopic.TOPIC,
                (FileTopic) this::addFile);

        contentPanel.onChatMemoryCleared(userMessageTextField::requestFocusInWindow);
        contentPanel.setBackground(new JBColor(0xE0EEF7, 0x2F2F2F)); // 设置背景颜色
        contentPanel.setPreferredSize(new Dimension(0, 50)); // 设置面板高度

        splitter.setFirstComponent(contentPanel);
        splitter.setSecondComponent(actionPanel);
    }

    private void addFile() {
        InputContext inputContext = ChatLink.forProject(project).getInputContext();
        filePanel.removeAll();
        if (inputContext.getAttachments().isEmpty()) {
            return;
        }
        filePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        filePanel.setVisible(true);

        for (PromptAttachment attachment : ChatLink.forProject(project).getInputContext().getAttachments()) {
            addFileCard(filePanel, attachment);
        }

    }

    private void addFileCard(JPanel filePanel, PromptAttachment attachment) {
        // 创建文件卡片
        JPanel fileCard = new JPanel();
        fileCard.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        fileCard.setLayout(new GridBagLayout()); // 使用GridBagLayout

        // 添加文件名标签
        JLabel fileLabel = new JLabel(attachment.getName());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; // 设置位置为(0, 0)
        gbc.gridy = 0; // 设置位置为(0, 0)
        gbc.anchor = GridBagConstraints.WEST; // 左对齐
        fileCard.add(fileLabel, gbc);

        // 创建删除图标按钮
        JButton deleteButton = new JButton();
        deleteButton.setIcon(AllIcons.Actions.Close); // 替换为你的图标路径
        deleteButton.setPreferredSize(new Dimension(20, 20)); // 设置按钮大小
        deleteButton.setBorderPainted(false); // 不显示边框
        deleteButton.setContentAreaFilled(false); // 不填充背景
        deleteButton.setFocusPainted(false); // 不显示焦点

        // 设置删除按钮位置
        gbc.gridx = 1; // 设置位置为(1, 0)
        gbc.gridy = 0; // 设置位置为(0, 0)
        gbc.anchor = GridBagConstraints.NORTHEAST; // 右上角对齐
        fileCard.add(deleteButton, gbc);

        // 添加图标按钮的动作监听器
        deleteButton.addActionListener(e -> {
            // 从文件面板中移除文件卡片
            ChatLink.forProject(project).getInputContext().removeAttachment(attachment);
            filePanel.remove(fileCard);
            filePanel.revalidate(); // 更新布局
            filePanel.repaint();    // 重绘面板
        });

        // 将文件卡片添加到文件面板
        filePanel.add(fileCard);
    }

    /**
     * 创建操作面板的方法
     */
    private JPanel createActionPanel() {
        JPanel panel = new RoundedPanel(10); // 创建圆角面板
        panel.setBackground(new JBColor(0xE0EEF7, 0x2d2f30)); // 设置背景颜色
        // 设置滚动面板
        JScrollPane scrollPane = new JScrollPane(userMessageTextField);
        scrollPane.setPreferredSize(new Dimension(userMessageTextField.getWidth(), 60));
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); // 无边框
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        // 隐藏滚动条
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0)); // 设置滚动条宽度为0
        scrollPane.getVerticalScrollBar().setVisible(false); // 隐藏滚动条

        // 允许鼠标滚动
        scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER); // 添加输入区域
        panel.add(submitButton, BorderLayout.EAST); // 添加发送按钮
        return panel;
    }

    // 创建输入区域的方法
    private JBTextArea createPromptTextArea(SubmitListener listener) {
        JBTextArea textArea = new JBTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setMargin(new Insets(5, 5, 5, 5)); // 设置边距
        textArea.addKeyListener(listener); // 添加键盘监听器
        textArea.setOpaque(false); // 设置透明

        return textArea;
    }

    // 创建停止生成按钮的方法
    private JButton createStopButton() {
        JButton stopButton = new JButton(AllIcons.Actions.Suspend);
        stopButton.setContentAreaFilled(false); // 去掉背景
        stopButton.setBorderPainted(false); // 去掉边框

        stopButton.addActionListener(e -> {
            aroundRequest(false);
            if (requestHolder instanceof Disposable disposable) {
                disposable.dispose();
            } else if (requestHolder instanceof Subscription subscription) {
                subscription.cancel();
            }
        });
        setButtonMouseListener(stopButton); // 设置鼠标监听器
        return stopButton;
    }


    // 创建发送按钮的方法
    private JButton createSendButton(SubmitListener listener) {
        JButton sendButton = new JButton(ThemeTool.isDark() ? AIIcons.SEND_DARK : AIIcons.SEND_LIGHT);
        sendButton.setContentAreaFilled(false); // 去掉背景
        sendButton.setBorderPainted(false); // 去掉边框
        sendButton.addActionListener(listener); // 添加动作监听器
        setButtonMouseListener(sendButton); // 设置鼠标监听器
        return sendButton;
    }

    private void setButtonMouseListener(JButton button) {
        button.setUI(new DarculaButtonUI()); // 设置UI风格
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // 设置小手光标
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setCursor(Cursor.getDefaultCursor()); // 恢复默认光标
            }
        });

    }

    @Override
    public final ChatLink getChatLink() {
        return chatLink;
    }

    public ModelType getModelType() {
        return getChatLink().getConversationContext().getModelType();
    }

    @Override
    public void exchangeStarting(ChatMessageEvent.Starting event) throws ChatExchangeAbortException {
        if (!presetCheck()) {
            throw new ChatExchangeAbortException("Preset check failed");
        }

        ApplicationManager.getApplication().invokeAndWait(() ->
                answer = new ConversationTurnPanel(new AssistantMessage("Waiting for AI..."), getModelType()));
        SwingUtilities.invokeLater(() -> {
            setSearchText("");
            aroundRequest(true);

            ConversationPanel contentPanel = getContentPanel();
            contentPanel.add(new ConversationTurnPanel(event.getUserMessage(), null));
            contentPanel.add(answer);
        });
    }

    private volatile ConversationTurnPanel answer;

    @Override
    public void exchangeStarted(ChatMessageEvent.Started event) {
        setRequestHolder(event.getSubscription());

        SwingUtilities.invokeLater(contentPanel::updateLayout);
    }

    protected boolean presetCheck() {
        var settings = GeneralSettings.getInstance();
        var assistantType = getChatLink().getConversationContext().getAssistantType();
        var options = settings.getAssistantOptions(assistantType);
        var family = assistantType.getFamily();

        // 如果该助手类型的家族允许 API Key 可选，直接返回 true
        if (family.isApiKeyOptional()) {
            return true;
        }

        // Azure OpenAI 特殊处理
        if (family == ModelFamily.AZURE_OPENAI) {
            return presetCheckForAzure(assistantType, options);
        }

        // 检查 QIANFAN 的 AccessKey 和 SecretKey 是否为空
        if (family == ModelFamily.QIANFAN) {
            if (isEmpty(options.getAccessKey()) || isEmpty(options.getSecretKey())) {
                sendNotification(Bundle.get("notify.config.apikey.text"), family.getApiKeysHomepage());
                return false;
            }
            return true;
        }

        // 检查 ApiKey 是否为空
        if (isEmpty(options.getApiKey())) {
            sendNotification(Bundle.get("notify.config.apikey.text"), family.getApiKeysHomepage());
            return false;
        }

        return true;
    }

    /**
     * 发送通知，提醒用户配置 API Key。
     *
     * @param message  通知信息
     * @param homepage API Key 的主页链接
     */
    private void sendNotification(String message, String homepage) {
        Notification notification = new Notification(
                Bundle.get("group.id"),
                Bundle.get("notify.config.title"),
                message,
                NotificationType.ERROR
        );

        notification.addAction(new SettingAction(Bundle.get("notify.config.action.config"), true));
        notification.addAction(new BrowseNotificationAction(Bundle.get("notify.config.action.browse"), homepage));

        Notifications.Bus.notify(notification);
    }

    protected boolean presetCheckForAzure(AssistantType assistantType, GeneralSettings.AssistantOptions options) {
        var apiKey = options.getApiKey();
        var apiEndpoint = options.getAzureApiEndpoint();
        var deploymentName = options.getAzureDeploymentName();

        if (isEmpty(apiKey) || isEmpty(apiEndpoint) || isEmpty(deploymentName)) {
            var missingOpts = new ArrayList<String>();
            if (isEmpty(apiKey)) missingOpts.add("\"API Key\"");
            if (isEmpty(apiEndpoint)) missingOpts.add("\"API Endpoint\"");
            if (isEmpty(deploymentName)) missingOpts.add("\"Deployment Name\"");

            Notification notification = new Notification(Bundle.get("group.id"),
                    Bundle.get("notify.config.title"),
                    Bundle.get("notify.config.opts.text", String.join(", ", missingOpts)),
                    NotificationType.ERROR);
            notification.addAction(new SettingAction(Bundle.get("notify.config.action.config"), true));
            notification.addAction(new BrowseNotificationAction(Bundle.get("notify.config.action.browse"), assistantType.getFamily().getApiKeysHomepage()));
            Notifications.Bus.notify(notification);
            return false;
        }
        return true;
    }

    @Override
    public void responseArriving(ChatMessageEvent.ResponseArriving event) {
        setContent(event.getPartialResponseChoices());
    }

    @Override
    public void responseArrived(ChatMessageEvent.ResponseArrived event) {
        setContent(event.getGenerations());

        Usage usage = event.getResponse().getMetadata().getUsage();
        SwingUtilities.invokeLater(() -> {
            aroundRequest(false);
            contentPanel.updateUsage(usage, getChatLink().getConversationContext().getModelType());
        });
    }

    public void setContent(List<Generation> content) {
        if (!content.isEmpty()) {
            var generation = content.get(0);
            answer.setContent(generation.getOutput(), ChatCompletionParser.parseTextContent(generation));
        }
    }

    @Override
    public void exchangeFailed(ChatMessageEvent.Failed event) {
        if (answer != null) {
            answer.setErrorContent(Errors.getWebClientErrorMessage(event.getCause()));
        }
        aroundRequest(false);
    }

    @Override
    public void exchangeCancelled(ChatMessageEvent.Cancelled event) {

    }

    public String getSearchText() {
        return userMessageTextField.getText();
    }

    public void setSearchText(String t) {
        userMessageTextField.setText(t);
    }

    public JPanel init() {
        return splitter;
    }

    public void aroundRequest(boolean status) {
        filePanel.setVisible(false);
        submitButton.setEnabled(!status);
        if (status) {
            actionPanel.remove(submitButton);
            actionPanel.add(stopGenerating, BorderLayout.EAST);
        } else {
            actionPanel.remove(stopGenerating);
            actionPanel.add(submitButton, BorderLayout.EAST);
        }
        actionPanel.revalidate();
        actionPanel.repaint();
    }

}
