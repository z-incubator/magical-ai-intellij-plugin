
package com.ai.gui.tool.window;

import com.ai.i18n.Bundle;
import com.ai.chat.messages.MediaSupport;
import com.intellij.icons.AllIcons.General;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import org.springframework.ai.model.Media;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.intellij.util.ui.JBUI.Panels.simplePanel;

public class CollapsibleImagePanel extends JPanel {

    public CollapsibleImagePanel(List<Media> images) {
        super(new BorderLayout());
        setOpaque(false);

        List<JPanel> contentPanels = new ArrayList<>();
        add(createToggleButton(contentPanels, images.size()), BorderLayout.NORTH);

        int n = 1;
        for (Media image : images) {
            var contentPanel = createContentPanel(Bundle.get("image.n", n), (byte[]) image.getData());
            add(contentPanel, BorderLayout.CENTER);
            contentPanels.add(contentPanel);
        }
    }

    private JPanel createContentPanel(String fileName, byte[] imageData) {
        var panel = new JPanel();
        panel.setOpaque(false);
        panel.setVisible(true);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(JBUI.Borders.empty(4, 0));
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData);
            BufferedImage originalImage = ImageIO.read(inputStream);
            int MAX_WIDTH = 360;
            int MAX_HEIGHT = 120;
            BufferedImage resizedImage = MediaSupport.resizeImage(originalImage, MAX_WIDTH, MAX_HEIGHT);
            panel.add(simplePanel()
                    .andTransparent()
                    .addToTop(
                            new JBLabel("<html><small><strong>%s</strong></small></html>".formatted(fileName))
                                    .withBorder(JBUI.Borders.emptyBottom(4)))
                    .addToLeft(new JBLabel(new ImageIcon(resizedImage))), BorderLayout.LINE_START);
        } catch (IOException e) {
            panel.add(new JBLabel("ERROR: Something went wrong while reading the image"));
            throw new UncheckedIOException(e);
        }
        return panel;
    }

    private JToggleButton createToggleButton(Collection<JPanel> contentPanels, int numImages) {
        var toggleButton = new JToggleButton(
                Bundle.get(numImages == 1 ? "ui.attachedImage" : "ui.attachedImages", numImages),
                General.ArrowDown);
        toggleButton.setFocusPainted(false);
        toggleButton.setContentAreaFilled(false);
        toggleButton.setBackground(getBackground());
        toggleButton.setSelectedIcon(General.ArrowUp);
        toggleButton.setBorder(null);
        toggleButton.setSelected(true);
        toggleButton.setHorizontalAlignment(SwingConstants.LEADING);
        toggleButton.setHorizontalTextPosition(SwingConstants.LEADING);
        toggleButton.addItemListener(e -> contentPanels.forEach(p -> p.setVisible(e.getStateChange() == ItemEvent.SELECTED)));

        return toggleButton;
    }
}
