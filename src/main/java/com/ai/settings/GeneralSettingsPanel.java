package com.ai.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.ai.chat.AssistantType;
import com.ai.settings.state.GeneralSettings;
import com.ai.topic.OpenAITopic;
import com.ai.i18n.Bundle;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class GeneralSettingsPanel implements Configurable {
    private JPanel myMainPanel;
    private JBTextField readTimeoutField;
    private JCheckBox enableAvatarCheckBox;
    private JComboBox<String> defaultModelComboBox;
    private JBTextField systemRoleField;
    private JCheckBox enableInitialMessageCheckBox;
    private JLabel readTimeoutHelpLabel;
    private JLabel contentOrderHelpLabel;
    private boolean needRestart = false;

    public GeneralSettingsPanel() {
        setupUI();
    }

    @Override
    public void reset() {
        GeneralSettings state = GeneralSettings.getInstance();
        readTimeoutField.setText(state.getReadTimeout());
        enableAvatarCheckBox.setSelected(state.isEnableAvatar());
        defaultModelComboBox.setSelectedItem(state.defaultModel);
        systemRoleField.setText(state.roleText);
        enableInitialMessageCheckBox.setSelected(Boolean.TRUE.equals(state.getEnableInitialMessage()));
        initHelp();
    }

    @Override
    public @Nullable JComponent createComponent() {
        return myMainPanel;
    }

    @Override
    public boolean isModified() {
        GeneralSettings state = GeneralSettings.getInstance();

        // If you change the order, you need to restart the IDE to take effect
        needRestart = !StringUtil.equals(state.defaultModel, (String) defaultModelComboBox.getSelectedItem());

        return !StringUtil.equals(state.getReadTimeout(), readTimeoutField.getText())
                || !state.isEnableAvatar() == enableAvatarCheckBox.isSelected()
                || !StringUtil.equals(state.defaultModel, (String) defaultModelComboBox.getSelectedItem())
                || !state.roleText.equals(systemRoleField.getText())
                || !Boolean.TRUE.equals(state.getEnableInitialMessage()) == enableInitialMessageCheckBox.isSelected();
    }

    @Override
    public void apply() {
        GeneralSettings state = GeneralSettings.getInstance();

        boolean readTimeoutIsNumber = StringUtils.isNumeric(readTimeoutField.getText());
        state.setReadTimeout(!readTimeoutIsNumber ? "50000" : readTimeoutField.getText());
        state.setEnableAvatar(enableAvatarCheckBox.isSelected());
        state.defaultModel = (String) defaultModelComboBox.getSelectedItem();
        state.roleText = systemRoleField.getText();
        state.setEnableInitialMessage(enableInitialMessageCheckBox.isSelected());

        if (needRestart) {
            try {
                @NotNull Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
                Arrays.stream(openProjects)
                        .forEach(project -> project.getMessageBus()
                                .syncPublisher(OpenAITopic.TOPIC)
                                .change(AssistantType.System.valueOf(state.defaultModel)));
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public String getDisplayName() {
        return Bundle.get("ui.setting.menu.text");
    }

    public void setupUI() {

        myMainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);

        // Connection Settings
        JPanel connectionTitledBorderBox = new JPanel(new GridBagLayout());
        TitledSeparator connectionSeparator = new TitledSeparator("Connection Settings");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 5;
        gbc.fill = GridBagConstraints.BOTH;
        myMainPanel.add(connectionSeparator, gbc);

        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        myMainPanel.add(connectionTitledBorderBox, gbc);

        // Read Timeout Label and Field
        JLabel readTimeoutLabel = new JLabel("Read Timeout: ");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        myMainPanel.add(readTimeoutLabel, gbc);

        readTimeoutField = new JBTextField();
        readTimeoutField.getEmptyText().setText(Bundle.get("ui.setting.connection.read_timeout.empty_text"));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.ipadx = 150;
        myMainPanel.add(readTimeoutField, gbc);

        // Read Timeout Help Label
        readTimeoutHelpLabel = new JLabel(Bundle.get("ui.setting.connection.read_timeout.remark"));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        myMainPanel.add(readTimeoutHelpLabel, gbc);

        // Tool Window Settings
        JPanel contentTitledBorderBox = new JPanel(new GridBagLayout());
        TitledSeparator contentSeparator = new TitledSeparator("Other Settings");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 5;
        gbc.fill = GridBagConstraints.BOTH;
        myMainPanel.add(contentSeparator, gbc);

        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        myMainPanel.add(contentTitledBorderBox, gbc);

        // Content Order Help Label
        contentOrderHelpLabel = new JLabel("Customize the order of Content according to your habits");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 5;
        gbc.anchor = GridBagConstraints.WEST;
        myMainPanel.add(contentOrderHelpLabel, gbc);

        // First ComboBox
        JLabel firstLabel = new JLabel("Default Model: ");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.EAST;
        myMainPanel.add(firstLabel, gbc);

        String[] items = Arrays.stream(AssistantType.System.values())
                .map(AssistantType.System::name)
                .toArray(String[]::new);
        defaultModelComboBox = new JComboBox<>(items);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        myMainPanel.add(defaultModelComboBox, gbc);

        // System role field
        JLabel roleLabel = new JLabel("Role: ");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.anchor = GridBagConstraints.EAST;
        myMainPanel.add(roleLabel, gbc);

        systemRoleField = new JBTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 8;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        myMainPanel.add(systemRoleField, gbc);

        // Enable Avatar Checkbox
        enableAvatarCheckBox = new JCheckBox("Enable avatar");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 5;
        gbc.anchor = GridBagConstraints.WEST;
        myMainPanel.add(enableAvatarCheckBox, gbc);

        // Enable Initial Message Checkbox
        enableInitialMessageCheckBox = new JCheckBox("Enable initial message");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.gridwidth = 5;
        gbc.anchor = GridBagConstraints.WEST;
        myMainPanel.add(enableInitialMessageCheckBox, gbc);


        gbc.gridx = 0;
        gbc.gridy = 11;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 0.0;
        gbc.weighty = 1.0; // Keep the button size fixed

        JPanel auto = new JPanel();
        myMainPanel.add(auto, gbc);
    }

    public void initHelp() {
        // Set font and color for help labels
        readTimeoutHelpLabel.setFont(JBUI.Fonts.smallFont());
        readTimeoutHelpLabel.setForeground(UIUtil.getContextHelpForeground());

        contentOrderHelpLabel.setFont(JBUI.Fonts.smallFont());
        contentOrderHelpLabel.setForeground(UIUtil.getContextHelpForeground());
    }
}