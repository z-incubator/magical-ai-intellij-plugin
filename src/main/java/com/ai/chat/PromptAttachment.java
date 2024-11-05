
package com.ai.chat;

import com.ai.text.TextContent;
import org.springframework.ai.model.Media;

import javax.swing.*;
import java.util.Optional;
import java.util.function.ToIntFunction;

public interface PromptAttachment {

    Optional<TextContent> getTextContentIfPresent();

    Optional<Media> getMediaContentIfPresent();

    Icon getIcon();

    String getName();

    boolean isPinned();

    void setPinned(boolean pinned);

    int getEstimatedTokenCount(ToIntFunction<? super PromptAttachment> estimator);

}
