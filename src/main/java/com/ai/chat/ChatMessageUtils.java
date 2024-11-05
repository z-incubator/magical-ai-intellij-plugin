
package com.ai.chat;

import com.ai.chat.messages.MessageSupport;
import com.ai.core.TextSubstitutor;
import com.ai.text.CodeFragment;
import com.ai.text.TextContent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;

import java.util.List;

public class ChatMessageUtils {

    public static List<TextContent> composeExcept(List<TextContent> textContents, List<? extends TextContent> exceptions, String exceptionPrompt) {
        for (var codeFragment : textContents)
            if (!exceptions.contains(codeFragment) && !exceptionPrompt.contains(TextContent.toString(codeFragment).strip()))
                return textContents;

        return List.of();
    }

    public static String composeAll(String prompt, List<? extends TextContent> textContents) {
        var buf = new StringBuilder();
        for (var textContent : textContents) {
            if (prompt.contains(textContent.toString()))
                continue;
            if (textContent instanceof CodeFragment codeFragment && StringUtils.isEmpty(codeFragment.description()))
                buf.append("[Selected code]\n");
            textContent.appendTo(buf);
            buf.append("\n\n");
        }
        if (!prompt.isEmpty()) {
            if (!buf.isEmpty())
                buf.append("\n\n");
            buf.append(prompt);
        }

        return buf.toString();
    }

    public static boolean isRoleUser(Message chatMessage) {
        return isRole(MessageType.USER, chatMessage);
    }

    public static boolean isRoleSystem(Message chatMessage) {
        return isRole(MessageType.SYSTEM, chatMessage);
    }

    private static boolean isRole(MessageType type, Message chatMessage) {
        return type == chatMessage.getMessageType();
    }

    @SuppressWarnings("StringEquality")
    public static void substitutePlaceholders(List<Message> chatMessages, TextSubstitutor substitutor) {
        chatMessages.replaceAll(chatMessage -> {
            String template = chatMessage.getContent();
            String resolved = substitutor.resolvePlaceholders(template);
            if (resolved != template) {
                chatMessage = MessageSupport.setContent(chatMessage, resolved);
            }
            return chatMessage;
        });
    }

    public static int countTokens(List<Message> messages) {
        return messages.size();
    }
}
