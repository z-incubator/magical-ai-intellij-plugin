
package com.ai.chat.messages;

import org.springframework.ai.chat.messages.*;

public class MessageSupport {

    private static final String ELLIPSIS = "[...]";

    public static Message substring(Message message, int fromIndex) {
        return setContent(message, ELLIPSIS + " " + message.getContent().substring(fromIndex));
    }

    /**
     * Sets new content to a given {@link Message} and returns a new instance of the message with updated content.
     * This method supports updating content for {@link UserMessage}, {@link SystemMessage}, and {@link AssistantMessage}.
     * It preserves other properties of the message, such as media and metadata, where applicable.
     *
     * @param message The original message object whose content is to be updated. This object is not modified.
     * @param content The new content string to be set in the message.
     * @return A new instance of the message with the updated content. The specific type of the returned message
     *         corresponds to the type of the input message.
     * @throws IllegalArgumentException If the input message type is not supported by this method, an exception
     *                                  is thrown indicating the unsupported message type.
     */
    public static Message setContent(Message message, String content) {
        if (message instanceof UserMessage userMessage) {
            return new UserMessage(content, userMessage.getMedia(), userMessage.getMetadata());
        } else if (message instanceof SystemMessage) {
            return new SystemMessage(content);
        } else if (message instanceof AssistantMessage) {
            return new AssistantMessage(content, message.getMetadata());
        } else if (message instanceof ToolResponseMessage toolMessage) {
            return new ToolResponseMessage(toolMessage.getResponses(), toolMessage.getMetadata());
        } else {
            throw new IllegalArgumentException("Unsupported message type: " + message.getClass().getSimpleName());
        }
    }
}
