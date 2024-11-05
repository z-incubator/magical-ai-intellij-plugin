
package com.ai.chat;

import com.ai.text.TextContent;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.model.Media;

import java.util.List;

public interface ChatMessageComposer {

    UserMessage compose(ConversationContext ctx, String textContent, List<Media> mediaList);

    UserMessage compose(ConversationContext ctx, String userPrompt, List<TextContent> textContents, List<Media> mediaList);

}
