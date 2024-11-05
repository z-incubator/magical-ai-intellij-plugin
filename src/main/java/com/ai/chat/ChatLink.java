
package com.ai.chat;

import com.ai.text.TextContent;
import com.ai.gui.tool.window.ChatToolWindow;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;

import java.util.List;

public interface ChatLink {

    Key<ChatLink> KEY = Key.create("ChatLink.current");

    static ChatLink forProject(Project project) {
        ChatToolWindow.ensureActivated(project);
        return project.getUserData(ChatLink.KEY);
    }

    Project getProject();

    InputContext getInputContext();

    ConversationContext getConversationContext();

    void pushMessage(String prompt, List<? extends TextContent> textContents);

    void addChatMessageListener(ChatMessageListener listener);

    void removeChatMessageListener(ChatMessageListener listener);

    default void regenerateResponse() {

    }
}
