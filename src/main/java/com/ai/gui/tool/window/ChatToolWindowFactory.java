package com.ai.gui.tool.window;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManagerEvent;
import com.intellij.ui.content.ContentManagerListener;
import com.ai.chat.AssistantType;
import com.ai.chat.ChatLink;
import com.ai.chat.ChatLinkProvider;
import com.ai.gui.browser.BrowserContent;
import com.ai.settings.state.GeneralSettings;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatToolWindowFactory {

    public static final Key<AssistantType> ACTIVE_TAB = Key.create("MagicalAI.ChatToolWindow.ACTIVE_TAB");

    private static final Key<Map<AssistantType, AssistantTool>> CONTENT_MAP = Key.create("MagicalAI.ChatToolWindow.CONTENT_MAP");
    private static final String ACTIVE_CONTENT_KEY = "MagicalAI.chatgpt.ToolWindow.ACTIVE";
    private static final Logger log = Logger.getInstance(ChatToolWindowFactory.class);
    private static final String AI = "AI";

    private static void setContentMap(ToolWindow toolWindow, Map<AssistantType, AssistantTool> tools) {
        toolWindow.getProject().putUserData(CONTENT_MAP, tools);
    }

    private static Map<AssistantType, AssistantTool> getContentMap(ToolWindow toolWindow) {
        return toolWindow.getProject().getUserData(CONTENT_MAP);
    }

    public static void addToolWindowContent(ToolWindow toolWindow, AssistantType type, GeneralSettings settings) {
        var contentFactory = new AssistantToolFactory(toolWindow.getProject(), settings, ContentFactory.getInstance());
        var contentMap = getContentMap(toolWindow);

        addToolWindowContent(toolWindow, type, contentFactory, contentMap);
    }

    private static void addToolWindowContent(
            ToolWindow toolWindow,
            AssistantType type,
            AssistantToolFactory contentFactory,
            Map<AssistantType, AssistantTool> contentMap) {
        try {
            AssistantTool assistant = contentFactory.createAssistantTool(type);
            toolWindow.getContentManager().addContent(assistant.content());
            contentMap.put(type, assistant);
        } catch (Exception | Error e) {
            if (type == AssistantType.System.OnlineTool)
                log.warn("'ChatGPT Online' is disabled due to: " + e.getMessage());
            else
                throw e;
        }
    }


    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        GeneralSettings settings = GeneralSettings.getInstance();
        AssistantToolFactory contentFactory = new AssistantToolFactory(project, settings, ContentFactory.getInstance());

        Map<AssistantType, AssistantTool> contentMap = new ConcurrentHashMap<>();
        for (var type : AssistantType.System.values()) {
            if (!type.isEnabled(settings))
                continue;

            addToolWindowContent(toolWindow, type, contentFactory, contentMap);
        }
        setContentMap(toolWindow, contentMap);

        // Set the default component. It require the 1st container
        String activeContent = PropertiesComponent.getInstance().getValue(ACTIVE_CONTENT_KEY, GeneralSettings.getInstance().defaultModel);
        try {
            AssistantType.System activeContentKey = AssistantType.System.valueOf(activeContent);
            var content = contentMap.get(activeContentKey);
            if (content != null) {
                project.putUserData(ChatLink.KEY, content.getChatLink());
            }
        } catch (Exception ignored) {
        }

        // Add the selection listener
        toolWindow.addContentManagerListener(new ContentManagerListener() {
            @Override
            public void selectionChanged(@NotNull ContentManagerEvent event) {
                var assistantType = event.getContent().getUserData(ACTIVE_TAB);
                if (assistantType instanceof AssistantType.System system) {
                    var content = contentMap.get(system);
                    if (content != null) {
                        project.putUserData(ChatLink.KEY, content.getChatLink());
                    }
                }
                PropertiesComponent.getInstance(project).setValue(ACTIVE_CONTENT_KEY, (assistantType == null) ? null : assistantType.name());
            }
        });
    }

    private static class AssistantToolFactory {
        private final Project project;
        private final GeneralSettings settings;
        private final ContentFactory contentFactory;

        AssistantToolFactory(Project project, GeneralSettings settings, ContentFactory contentFactory) {
            this.project = project;
            this.settings = settings;
            this.contentFactory = contentFactory;
        }

        AssistantTool createAssistantTool(AssistantType type) {
            ChatLinkProvider provider;
            Content content;
            if (type == AssistantType.System.OnlineTool) {
                BrowserContent browser = new BrowserContent(project);
                content = contentFactory.createContent(browser.getContentPanel(), AI, false);
                provider = browser;
            } else {
                ChatPanel chatPanel = new ChatPanel(project, settings.getAssistantOptions(type));
                content = contentFactory.createContent(chatPanel.init(), AI, false);
                provider = chatPanel;
            }
            content.putUserData(ACTIVE_TAB, type);
            content.setCloseable(false);

            return new AssistantTool(provider, content);
        }
    }

    record AssistantTool(ChatLinkProvider provider, Content content) {

        public ChatLink getChatLink() {
            return provider.getChatLink();
        }
    }
}
