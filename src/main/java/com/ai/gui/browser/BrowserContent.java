package com.ai.gui.browser;

import com.ai.chat.*;
import com.ai.gui.browser.action.*;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.project.Project;
import com.intellij.ui.jcef.JBCefApp;
import com.intellij.ui.jcef.JBCefBrowser;
import com.intellij.util.ui.JBUI;
import com.ai.settings.state.GeneralSettings;
import com.ai.topic.OnlineTopic;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 浏览器内容
 */
public class BrowserContent implements ChatLinkProvider {
    @Getter
    private final JPanel contentPanel;
    private JBCefBrowser browser;
    private final ChatLink chatLink;

    public BrowserContent(Project project) {
        this(project, GeneralSettings.getInstance().getOnlineUrl());
    }

    public BrowserContent(Project project, String url) {
        contentPanel = new JPanel(new BorderLayout());
        chatLink = new ChatLinkService(project, new BrowserConversationHandler(), null);

        if (!JBCefApp.isSupported()) {
            String message = "The current IDE does not support Online ChatGPT, because the JVM RunTime does not support JCEF.\n";
            JTextPane area = new JTextPane();
            area.setEditable(false);
            area.setText(message);
            area.setBorder(JBUI.Borders.empty(10));
            contentPanel.add(area, BorderLayout.CENTER);
            return;
        }
        browser = new JBCefBrowser(url);
        browser.setZoomLevel(browser.getZoomLevel() - 0.1D);
        AtomicReference<JComponent> component = new AtomicReference<>(browser.getComponent());
        DefaultActionGroup toolbarActions = new DefaultActionGroup();
        toolbarActions.add(new RefreshPage(browser));
        toolbarActions.add(new Separator());
        toolbarActions.add(new ZoomLevelAdd(browser));
        toolbarActions.add(new ZoomLevelSub(browser));
        toolbarActions.add(new Separator());
        toolbarActions.add(new ClearCookies(browser, contentPanel));
        toolbarActions.add(new OnlineURLAction());  // 将下拉框添加到工具栏

        ActionToolbar browserToolbar = ActionManager.getInstance().createActionToolbar("Browser Toolbar", toolbarActions, true);
        browserToolbar.setTargetComponent(null);

        contentPanel.add(browserToolbar.getComponent(), BorderLayout.NORTH);
        contentPanel.add(component.get(), BorderLayout.CENTER);

        project.getMessageBus().connect().subscribe(OnlineTopic.TOPIC,
                (OnlineTopic) this::execute);
    }

    public void execute(String question) {
        question = question.replace("'", "\\'");

        String fillQuestion = "document.getElementsByTagName(\"textarea\")[0].value = '" + question + "'";
        String enableButton = "document.getElementsByTagName(\"textarea\")[0].nextSibling.removeAttribute('disabled')";
        String doClick = "document.getElementsByTagName(\"textarea\")[0].nextSibling.click()";
        // Fill the question
        String formattedQuestion = fillQuestion.replace("\n", "\\n");
        browser.getCefBrowser().executeJavaScript(formattedQuestion, GeneralSettings.getInstance().getOnlineUrl(), 0);
        browser.getCefBrowser().executeJavaScript(enableButton, GeneralSettings.getInstance().getOnlineUrl(), 0);
        browser.getCefBrowser().executeJavaScript(doClick, GeneralSettings.getInstance().getOnlineUrl(), 0);
    }

    @Override
    public ChatLink getChatLink() {
        return chatLink;
    }

    private class BrowserConversationHandler implements ConversationHandler {

        @Override
        public void push(ConversationContext ctx, ChatMessageEvent.Starting event, ChatMessageListener listener) {
            execute(event.getUserMessage().getContent());
        }
    }
}

