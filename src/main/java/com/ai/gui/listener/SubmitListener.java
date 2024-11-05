
package com.ai.gui.listener;

import com.intellij.openapi.project.Project;
import com.ai.chat.ChatLink;
import com.ai.gui.ContextAwareAppetizer;
import com.ai.icons.AIIcons;
import com.ai.util.ThemeTool;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SubmitListener extends AbstractAction implements ActionListener, KeyListener, HyperlinkListener {

    private final ChatLink chatLink;
    private final Supplier<String> prompt;
    private final Consumer<String> setPrompt;

    private final ContextAwareAppetizer contextAwareAppetizer;

    public SubmitListener(ChatLink chatLink, Supplier<String> prompt, Consumer<String> setPrompt, ContextAwareAppetizer contextAwareAppetizer) {
        super("", ThemeTool.isDark() ? AIIcons.SEND_DARK : AIIcons.SEND_LIGHT);
        this.chatLink = chatLink;
        this.prompt = prompt;
        this.setPrompt = setPrompt;
        this.contextAwareAppetizer = contextAwareAppetizer;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        submitPrompt(prompt.get());
    }

    public void submitPrompt(String prompt) {
        Project project = chatLink.getProject();
        chatLink.pushMessage(prompt, contextAwareAppetizer.fetchSnippets(project));
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        // 捕获Shift + 回车键内容换行
        if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isShiftDown()) {
            e.consume(); // 防止默认行为
            this.setPrompt.accept(this.prompt.get() + "\n");
        }
        // 捕获回车键发送消息
        if (e.getKeyCode() == KeyEvent.VK_ENTER && !e.isControlDown() && !e.isShiftDown()) {
            e.consume();
            submitPrompt(prompt.get());
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            String prompt = extractPromptFromUrl(URI.create(e.getDescription()));
            if (!prompt.isEmpty())
                submitPrompt(prompt);
        }
    }

    public static String extractPromptFromUrl(URI uri) {
        final String PROMPT_QUERY_PARAM = "prompt=";
        if ("assistant".equals(uri.getScheme()) && uri.getQuery() != null && uri.getQuery().startsWith(PROMPT_QUERY_PARAM)) {
            String prompt = uri.getQuery().substring(PROMPT_QUERY_PARAM.length());
            return URLDecoder.decode(prompt, StandardCharsets.UTF_8);
        }
        return "";
    }
}
