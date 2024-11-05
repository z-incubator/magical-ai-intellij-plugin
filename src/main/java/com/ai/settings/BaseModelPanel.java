package com.ai.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.ui.TextFieldWithHistory;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.util.ui.UIUtil;
import com.ai.core.StartupHandler;
import com.ai.chat.AssistantType;
import com.ai.chat.client.ChatClientHolder;
import com.ai.chat.models.CustomModel;
import com.ai.chat.models.ModelType;
import com.ai.chat.models.StandardModel;
import com.ai.settings.state.GeneralSettings;
import com.ai.settings.state.GeneralSettings.AssistantOptions;
import com.ai.gui.tool.window.ChatToolWindow;
import com.ai.i18n.Bundle;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public abstract class BaseModelPanel implements Configurable, Configurable.Composite {
    protected JPanel mainPanel; // 主面板
    protected JPanel apiKeyTitledBorderBox; // API Key 面板
    protected JPanel modelTitledBorderBox; // 模型面板
    protected JPanel urlTitledBox; // URL 面板
    protected JPanel customizeServerOptions; // 自定义服务器选项面板
    protected JPanel customizeServerLabel; // 自定义服务器标签面板

    // UI 组件
    private JLabel apiKeyLabel; // API Key 标签
    protected JBPasswordField apiKeyField; // API Key 输入框
    protected JComboBox<String> comboCombobox; // 下拉框
    protected JCheckBox enableStreamResponseCheckBox; // 启用流响应复选框
    protected JCheckBox enableStreamOptionsCheckBox; // 启用流选项复选框
    protected JCheckBox enableCustomizeUrlCheckBox; // 启用自定义 URL 复选框
    protected TextFieldWithHistory customizeServerField; // 自定义服务器输入框
    private JSpinner temperatureSpinner; // 温度旋转框
    private JSpinner topPSpinner; // Top P 旋转框
    private JTextField azureApiEndpointField; // Azure API 端点输入框
    private JTextField azureDeploymentNameField; // Azure 部署名称输入框
    private JLabel azureApiEndpointLabel; // Azure API 端点标签
    private JLabel azureDeploymentNameLabel; // Azure 部署名称标签
    private JLabel modelLabel; // 模型标签

    // 相关字段
    private final AssistantType type; // 助手类型
    private final Predicate<ModelType> modelFilter; // 模型过滤器
    private List<CustomModel> apiModels; // API 模型列表

    // Ak/SK 相关字段
    private JLabel akLabel; // Ak 标签
    protected JTextField akField; // Ak 输入框
    private JLabel skLabel; // Sk 标签
    protected JBPasswordField skField; // Sk 输入框

    public BaseModelPanel(AssistantType type, Predicate<ModelType> modelFilter) {
        this.type = type; // 初始化助手类型
        this.modelFilter = modelFilter; // 初始化模型过滤器
        initComponents(); // 初始化组件
        init(); // 进行其他初始化
        configureAvailableModels(modelFilter); // 配置可用模型
    }

    private void initComponents() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        // 隐藏部分组件
        customizeServerOptions = new JPanel(new GridBagLayout());
        customizeServerLabel = new JPanel(new BorderLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5); // Spacing between components

        // API Key Section
        apiKeyLabel = new JLabel("API Key:");
        apiKeyField = new JBPasswordField();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.2;
        mainPanel.add(apiKeyLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.8;
        mainPanel.add(apiKeyField, gbc);

        // ak sk
        akLabel = new JLabel("AccessKey:");
        akField = new JTextField();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.2;
        mainPanel.add(akLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.8;
        mainPanel.add(akField, gbc);

        skLabel = new JLabel("SecretKey:");
        skField = new JBPasswordField();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.2;
        mainPanel.add(skLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.8;
        mainPanel.add(skField, gbc);

        // API Endpoint Section
        azureApiEndpointLabel = new JLabel("API Endpoint:");
        azureApiEndpointField = new JTextField();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.2;
        mainPanel.add(azureApiEndpointLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.8;
        mainPanel.add(azureApiEndpointField, gbc);

        // Deployment Name Section
        azureDeploymentNameLabel = new JLabel("Deployment name:");
        azureDeploymentNameField = new JTextField();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.2;
        mainPanel.add(azureDeploymentNameLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.8;
        mainPanel.add(azureDeploymentNameField, gbc);

        // Model Selection Section
        modelLabel = new JLabel("Model:");
        comboCombobox = new JComboBox<>();
        comboCombobox.setEditable(true);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.2;
        mainPanel.add(modelLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.8;
        mainPanel.add(comboCombobox, gbc);

        // Temperature Section
        JLabel temperatureLabel = new JLabel("Temperature:");
        temperatureSpinner = new JSpinner();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0.2;
        mainPanel.add(temperatureLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.8;
        mainPanel.add(temperatureSpinner, gbc);

        // Top P Section
        JLabel topPLabel = new JLabel("Top P:");
        topPSpinner = new JSpinner();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.weightx = 0.2;
        mainPanel.add(topPLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.8;
        mainPanel.add(topPSpinner, gbc);

        // Stream Response Options
        enableStreamResponseCheckBox = new JCheckBox("Enable stream response");
        enableStreamOptionsCheckBox = new JCheckBox("Enable stream options");
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        mainPanel.add(enableStreamResponseCheckBox, gbc);

        gbc.gridy = 8;
        mainPanel.add(enableStreamOptionsCheckBox, gbc);

        // Customize Server Section
        enableCustomizeUrlCheckBox = new JCheckBox("Customize Server");
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 1;
        mainPanel.add(enableCustomizeUrlCheckBox, gbc);

        customizeServerField = new TextFieldWithHistory();
        customizeServerField.setEnabled(false); // Initially disabled
        gbc.gridx = 1;
        mainPanel.add(customizeServerField, gbc);


        gbc.gridx = 0;
        gbc.gridy = 11;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 0.0;
        gbc.weighty = 1.0; // Keep the button size fixed
        JPanel auto = new JPanel();
        mainPanel.add(auto, gbc);


        Dimension fieldSize = new Dimension(300, 33);
        apiKeyField.setPreferredSize(fieldSize);
        akField.setPreferredSize(fieldSize);
        skField.setPreferredSize(fieldSize);
        azureApiEndpointField.setPreferredSize(fieldSize);
        azureDeploymentNameField.setPreferredSize(fieldSize);
        comboCombobox.setPreferredSize(fieldSize);
        temperatureSpinner.setPreferredSize(fieldSize);
        topPSpinner.setPreferredSize(fieldSize);
        customizeServerField.setPreferredSize(fieldSize);

        Dimension labelSize = new Dimension(50, 33);
        apiKeyLabel.setPreferredSize(labelSize);
        akLabel.setPreferredSize(labelSize);
        skLabel.setPreferredSize(labelSize);
        azureApiEndpointLabel.setPreferredSize(labelSize);
        azureDeploymentNameLabel.setPreferredSize(labelSize);
        modelLabel.setPreferredSize(labelSize);
        temperatureLabel.setPreferredSize(labelSize);
        topPLabel.setPreferredSize(labelSize);
        customizeServerLabel.setPreferredSize(labelSize);

        // Add listener to enable/disable fields based on checkbox
        enableCustomizeUrlCheckBox.addActionListener(e -> {
            boolean isSelected = enableCustomizeUrlCheckBox.isSelected();
            customizeServerField.setEnabled(isSelected);
        });
    }


    private void init() {
        if (!isApiKeyOptional()) {
            apiKeyField.getEmptyText().setText(Bundle.get("apiKey.missing", type.getFamily().getApiKeysHomepage()));
        }
        GeneralSettings state = GeneralSettings.getInstance();
        AssistantOptions config = getAssistantOptions(state);

        akField.setText(config.getAccessKey());
        skField.setText(config.getSecretKey());

        ItemListener proxyTypeChangedListener = e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                enableCustomizeServerOptions(true);
            } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                enableCustomizeServerOptions(false);
            }
        };
        enableCustomizeUrlCheckBox.addItemListener(proxyTypeChangedListener);
        enableCustomizeServerOptions(false);
        temperatureSpinner.setModel(new SpinnerNumberModel(0.4, 0.0, 2.0, 0.05));
        topPSpinner.setModel(new SpinnerNumberModel(0.95, 0.0, 1.0, 0.01));
        comboCombobox.setEditable(isModelNameEditable());
        comboCombobox.removeAllItems();
        configureAzureServerOptions();
        if (!isStreamOptionsApiAvailable()) {
            removeComponent(enableStreamOptionsCheckBox);
        }

        if (isQianfan()) {
            removeComponent(apiKeyLabel);
            removeComponent(apiKeyField);
        } else {
            // ak sk
            removeComponent(akLabel);
            removeComponent(akField);
            removeComponent(skLabel);
            removeComponent(skField);
        }
    }

    protected boolean isQianfan() {
        return false;
    }

    protected boolean isModelNameEditable() {
        return false;
    }

    protected boolean isApiKeyOptional() {
        return type.getFamily().isApiKeyOptional();
    }

    protected boolean isStreamOptionsApiAvailable() {
        return false;
    }

    public void setEnabledInToolWindow(boolean enabled) {
        if (type instanceof AssistantType.System system) {
            var enabledSet = GeneralSettings.getInstance().selectedModel();
            var changed = enabled ? enabledSet.add(system) : enabledSet.remove(system);

            if (changed && StartupHandler.isFullyStarted())
                ApplicationManager.getApplication().invokeLater(ChatToolWindow::synchronizeContents);
        }
    }

    public boolean isAzureCompatible() {
        return false;
    }

    protected void removeComponent(Component comp) {
        if (comp.getParent() != null)
            comp.getParent().remove(comp);
    }

    protected void configureAvailableModels(Predicate<ModelType> modelFilter) {
        comboCombobox.removeAllItems();
        StandardModel.getAvailableModels().stream()
                .filter(modelFilter)
                .forEach(model -> comboCombobox.addItem(model.id()));
    }

    protected void setAvailableModels(List<CustomModel> availableModels) {
        if (!availableModels.isEmpty()) {
            var selectedModel = comboCombobox.getSelectedItem();
            apiModels = availableModels;
            comboCombobox.removeAllItems();
            availableModels.forEach(model -> comboCombobox.addItem(model.id()));

            if (selectedModel != null) {
                comboCombobox.setSelectedItem(selectedModel);
            }
            if (comboCombobox.getSelectedIndex() == -1) {
                comboCombobox.setSelectedIndex(0);
            }
        }
    }

    protected void configureAzureServerOptions() {
        if (isAzureCompatible()) {
            customizeServerLabel.setVisible(false);
            customizeServerOptions.setVisible(false);
            comboCombobox.setVisible(false);
            modelLabel.setVisible(false);
        } else {
            azureApiEndpointLabel.setVisible(false);
            azureApiEndpointField.setVisible(false);
            azureDeploymentNameLabel.setVisible(false);
            azureDeploymentNameField.setVisible(false);
            removeComponent(azureApiEndpointLabel);
            removeComponent(azureApiEndpointField);
            removeComponent(azureDeploymentNameLabel);
            removeComponent(azureDeploymentNameField);
        }
    }

    private void enableCustomizeServerOptions(boolean enabled) {
        if (!enabled) {
            customizeServerField.setText(getAssistantOptions(GeneralSettings.getInstance()).getApiEndpointUrl());
        }
        UIUtil.setEnabled(customizeServerOptions, enabled, true);
    }

    protected AssistantOptions getAssistantOptions(GeneralSettings state) {
        return state.getAssistantOptions(type);
    }

    @Override
    public void reset() {
        GeneralSettings state = GeneralSettings.getInstance();
        AssistantOptions config = getAssistantOptions(state);
        setApiKeyMasked(apiKeyField, config);

        setAvailableModels(defaultIfNull(config.getApiModels(), List.of()));
        if (config.getApiModels().isEmpty()) {
            configureAvailableModels(modelFilter);
        }
        comboCombobox.setSelectedItem(config.getModelName());
        temperatureSpinner.setValue(config.getTemperature());
        topPSpinner.setValue(config.getTopP());
        enableStreamResponseCheckBox.setSelected(config.isEnableStreamResponse());
        enableStreamOptionsCheckBox.setSelected(config.isEnableStreamOptions());
        enableCustomizeUrlCheckBox.setSelected(config.isEnableCustomApiEndpointUrl());
        customizeServerField.setHistory(config.getApiEndpointUrlHistory());
        customizeServerField.setText(config.getApiEndpointUrl());
        azureApiEndpointField.setText(config.getAzureApiEndpoint());
        azureDeploymentNameField.setText(config.getAzureDeploymentName());

        akField.setText(config.getAccessKey());
        skField.getEmptyText().setText(config.getSecretKey());

    }

    @Override
    public @Nullable JComponent createComponent() {
        return mainPanel;
    }

    @Override
    public boolean isModified() {
        GeneralSettings state = GeneralSettings.getInstance();
        AssistantOptions config = getAssistantOptions(state);

        return apiKeyField.getPassword().length == 0 ||
                !config.getModelName().equals(comboCombobox.getSelectedItem()) ||
                skField.getPassword().length == 0 ||
                !StringUtils.equals(config.getAccessKey(), akField.getText()) ||
                !Double.valueOf(config.getTemperature()).equals(temperatureSpinner.getValue()) ||
                !Double.valueOf(config.getTopP()).equals(topPSpinner.getValue()) ||
                config.isEnableStreamResponse() != enableStreamResponseCheckBox.isSelected() ||
                config.isEnableStreamOptions() != enableStreamOptionsCheckBox.isSelected() ||
                config.isEnableCustomApiEndpointUrl() != enableCustomizeUrlCheckBox.isSelected() ||
                !config.getAzureApiEndpoint().equals(azureApiEndpointField.getText()) ||
                !config.getAzureDeploymentName().equals(azureDeploymentNameField.getText()) ||
                !config.getApiEndpointUrl().equals(customizeServerField.getText()) ||
                !defaultIfNull(config.getApiModels(), List.of()).equals(defaultIfNull(apiModels, List.of()));
    }

    @Override
    public void apply() {
        var settings = GeneralSettings.getInstance();
        var options = getAssistantOptions(settings);

        boolean isFirstUse = isEmpty(options.getApiKeyMasked());
        apply(options);
        maskApiKeyOnSave(options, isFirstUse);
        // 刷新缓存模型配置
        ChatClientHolder.refresh();
    }

    protected void apply(AssistantOptions config) {
        if (apiKeyField.getPassword().length > 0) {
            config.setApiKey(String.valueOf(apiKeyField.getPassword()));
        }
        config.setModelName(Objects.requireNonNull(comboCombobox.getSelectedItem()).toString());
        config.setTemperature((double) temperatureSpinner.getValue());
        config.setTopP((double) topPSpinner.getValue());
        config.setEnableStreamResponse(enableStreamResponseCheckBox.isSelected());
        config.setEnableStreamOptions(enableStreamOptionsCheckBox.isSelected());
        config.setEnableCustomApiEndpointUrl(enableCustomizeUrlCheckBox.isSelected());
        config.setApiEndpointUrl(customizeServerField.getText());
        config.setAzureApiEndpoint(azureApiEndpointField.getText());
        config.setAzureDeploymentName(azureDeploymentNameField.getText());
        if (!config.getApiEndpointUrlHistory().contains(config.getApiEndpointUrl()))
            customizeServerField.addCurrentTextToHistory();
        config.setApiEndpointUrlHistory(customizeServerField.getHistory());
        config.setApiModels(defaultIfNull(apiModels, List.of()));

        config.setAccessKey(akField.getText());
        config.setSecretKey(new String(skField.getPassword()));
    }

    private void maskApiKeyOnSave(AssistantOptions config, boolean isFirstUse) {
        if (apiKeyField.getPassword().length > 0) {
            setApiKeyMasked(apiKeyField, config);
            if (isFirstUse)
                setEnabledInToolWindow(true);
        }
    }

    private void setApiKeyMasked(JBPasswordField apiKeyField, AssistantOptions config) {
        apiKeyField.setText("");
        apiKeyField.getEmptyText().setText(config.getApiKeyMasked());
        if (config.getApiKeyMasked().isEmpty() && !isApiKeyOptional())
            apiKeyField.getEmptyText().setText(Bundle.get("apiKey.missing", type.getFamily().getApiKeysHomepage()));
    }

    @Override
    public String getDisplayName() {
        return Bundle.get("ui.setting.menu.text");
    }

    private void createUIComponents() {
        apiKeyTitledBorderBox = new JPanel(new BorderLayout());
        TitledSeparator tsUrl = new TitledSeparator("API Key Settings");
        apiKeyTitledBorderBox.add(tsUrl, BorderLayout.CENTER);

        modelTitledBorderBox = new JPanel(new BorderLayout());
        TitledSeparator mdUrl = new TitledSeparator("Other Settings");
        modelTitledBorderBox.add(mdUrl, BorderLayout.CENTER);

        urlTitledBox = new JPanel(new BorderLayout());
        TitledSeparator url = new TitledSeparator("Server Settings");
        urlTitledBox.add(url, BorderLayout.CENTER);
    }


    @Override
    public Configurable @NotNull [] getConfigurables() {
        return new Configurable[0];
    }
}
