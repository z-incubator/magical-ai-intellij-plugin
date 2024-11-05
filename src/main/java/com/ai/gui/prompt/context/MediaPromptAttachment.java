
package com.ai.gui.prompt.context;

import com.ai.text.TextContent;
import org.springframework.ai.model.Media;

import javax.swing.*;
import java.util.Optional;

public class MediaPromptAttachment extends AbstractPromptAttachment {

    private final Media media;

    public MediaPromptAttachment(Icon icon, String name, Media media) {
        super(icon, name);
        this.media = media;
    }

    @Override
    public Optional<TextContent> getTextContentIfPresent() {
        return Optional.empty();
    }

    @Override
    public Optional<Media> getMediaContentIfPresent() {
        return Optional.of(media);
    }
}
