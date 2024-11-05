
package com.ai.gui.prompt.context;

import com.ai.chat.PromptAttachment;
import com.ai.text.TextContent;
import org.springframework.ai.model.Media;

import javax.swing.*;
import java.util.Optional;
import java.util.function.ToIntFunction;

public class TextPromptAttachment extends AbstractPromptAttachment {
    private final TextContent content;


    public TextPromptAttachment(Icon icon, String name, TextContent content) {
        super(icon, name);
        this.content = content;
    }

    @Override
    public Optional<TextContent> getTextContentIfPresent() {
        return Optional.of(content);
    }

    @Override
    public Optional<Media> getMediaContentIfPresent() {
        return Optional.empty();
    }

    @Override
    protected int estimateTokenCount(ToIntFunction<? super PromptAttachment> estimator) {
        return estimator.applyAsInt(this);
    }
}
