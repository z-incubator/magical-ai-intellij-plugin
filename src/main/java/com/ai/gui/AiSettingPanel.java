package com.ai.gui;

import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import com.ai.gui.component.AiBaseComponent;
import com.ai.i18n.Bundle;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

/**
 * The {@code AiBaseSettingPanel} class represents a settings panel
 * with options for locale selection and large text input areas.
 */
public class AiSettingPanel extends JBPanel<AiSettingPanel> {

    private AiBaseComponent.State state;

    public AiSettingPanel() {
        super(new BorderLayout(0, 0));
        state = Objects.requireNonNull(AiBaseComponent.getInstance().getState());
        init();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                transferFocus();
            }
        });
    }

    /**
     * Initializes the components of the panel.
     */
    private void init() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = JBUI.insets(10, 20);

        // Locale panel
        gbc.gridy = 0;
        mainPanel.add(createLocalePanel(), gbc);
        JPanel auto = new JPanel();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 0.0;
        gbc.weighty = 1.0; // Keep the button size fixed
        mainPanel.add(auto, gbc);
        add(mainPanel, BorderLayout.CENTER);
    }

    /**
     * An {@code ItemListener} to handle locale change events.
     */
    private final ItemListener changeItemListener = itemEvent -> {
        if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
            state.setLocale(itemEvent.getItem().toString());

            SwingUtilities.invokeLater(() -> {
                boolean yes = MessageDialogBuilder.yesNo("Content order changed!", "Changing " +
                                "the content order requires restarting the IDE to take effect. Do you " +
                                "want to restart to apply the settings?")
                        .yesText("Restart")
                        .noText("Not Now").ask(this);
                if (yes) {
                    ApplicationManagerEx.getApplicationEx().restart(true);
                }
            });
        }
    };

    /**
     * Creates a panel with a locale selection combo box.
     *
     * @return a {@code JPanel} containing the locale selection component.
     */
    private @NotNull JPanel createLocalePanel() {
        JComboBox<String> languages = new JComboBox<>(new String[]{"中文", "English"});
        languages.setSelectedItem(state.getLocale());
        languages.addItemListener(changeItemListener);

        JLabel label = new JLabel(Bundle.get("ai.setting.language") + " ");
        label.setFont(label.getFont().deriveFont(Font.BOLD));

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Bundle.get("ai.setting.language")));
        panel.add(label, BorderLayout.WEST);
        panel.add(languages, BorderLayout.CENTER);
        String infoText = Bundle.get("ai.setting.language.info");
        JLabel infoLabel = new JLabel("<html><div style='width: 450px;color: gray;'>" + infoText + "</div></html>");

        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(infoLabel, BorderLayout.SOUTH);

        return panel;
    }
}
