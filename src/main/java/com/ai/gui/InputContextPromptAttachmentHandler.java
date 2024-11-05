
package com.ai.gui;

import com.ai.i18n.Bundle;
import com.ai.chat.InputContext;
import com.ai.chat.messages.MediaSupport;
import com.ai.gui.prompt.context.MediaPromptAttachment;
import com.intellij.icons.AllIcons;
import org.springframework.ai.model.Media;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class InputContextPromptAttachmentHandler implements PromptAttachmentHandler {

    private final InputContext context;

    public InputContextPromptAttachmentHandler(InputContext context) {
        this.context = context;
    }


    @Override
    public boolean handleTransferable(Transferable content) {
        try {
            if (content.isDataFlavorSupported(DataFlavor.imageFlavor) && content.getTransferData(DataFlavor.imageFlavor) instanceof RenderedImage image)
                return handleImageContent(image);

        } catch (IOException | UnsupportedFlavorException ignored) {
        }
        return false;
    }

    protected boolean handleImageContent(RenderedImage image) {
        var icon = AllIcons.Actions.AddFile;
        var media = MediaSupport.fromRenderedImageAsCompressedMedia(image);
        var attachment = new MediaPromptAttachment(icon, createPastedImageName(media), media);
        context.addAttachment(attachment);
        return true;
    }

    protected String createPastedImageName(Media media) {
        return Bundle.get("image.pasted.name", DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now()).replace('T', ' '));
    }
}
