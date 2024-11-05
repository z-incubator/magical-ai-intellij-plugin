
package com.ai.gui.tool.window;

import com.ai.i18n.Bundle;
import com.ai.chat.metadata.ImmutableUsage;
import com.ai.chat.models.ModelType;
import com.intellij.icons.AllIcons;
import com.intellij.ui.components.JBLabel;
import org.springframework.ai.chat.metadata.Usage;

import javax.swing.*;
import java.awt.*;

public class UsagePanel extends JPanel {
    private final JBLabel label;
    public UsagePanel() {
        super(new FlowLayout(FlowLayout.LEFT, 5, 0));
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        setOpaque(false);
        setVisible(false);
        this.label = createLabel();
        add(createHelpIcon(), BorderLayout.WEST);
        add(label, BorderLayout.CENTER);
    }

    public void updateUsage(Usage usage, ModelType model) {
        if (usage == null) {
            usage = ImmutableUsage.empty();
        }
        label.setText(createLabelText(usage, model));

        boolean notEmpty = usage.getTotalTokens() != null && !Long.valueOf(0L).equals(usage.getTotalTokens());
        if (isVisible() != notEmpty) {
            setVisible(notEmpty);
        }
    }

    private JBLabel createHelpIcon() {
        return new JBLabel(AllIcons.General.ContextHelp);
    }

    private JBLabel createLabel() {
        return new JBLabel(createLabelText(ImmutableUsage.empty(), null));
    }

    protected String createLabelText(Usage usage, ModelType model) {
        int inputTokenLimit = (model == null) ? Integer.MAX_VALUE : model.getInputTokenLimit();
        return String.format("<html><small>%s</small></html>",
                Bundle.get(
                        (inputTokenLimit == Integer.MAX_VALUE) ? "usage.in.out" : "usage.in.out.max",
                        usage.getPromptTokens(),
                        usage.getGenerationTokens(),
                        inputTokenLimit
                )
        );
    }
}
