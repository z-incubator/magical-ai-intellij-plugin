
package com.ai.core;

import com.ai.text.TextFragment;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.Generation;

public class ChatCompletionParser {

    public static TextFragment parseTextContent(Generation generation) {
        AssistantMessage assistantMessage = generation.getOutput();
        if (assistantMessage == null) {
            assistantMessage = new AssistantMessage("");
        }

        TextFragment parseResult = TextFragment.of(assistantMessage.getContent());
        parseResult.toHtml(); // pre-compute and cache HTML content in the current thread
        return parseResult;
    }
}
