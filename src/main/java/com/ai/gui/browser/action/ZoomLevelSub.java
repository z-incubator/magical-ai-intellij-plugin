package com.ai.gui.browser.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.jcef.JBCefBrowser;
import com.ai.util.ThemeTool;
import com.ai.icons.AIIcons;
import org.jetbrains.annotations.NotNull;


public class ZoomLevelSub extends AnAction {
    private final JBCefBrowser browser;

    public ZoomLevelSub(JBCefBrowser browser) {
        super(() -> "Zoom Level Reduced", ThemeTool.isDark() ? AIIcons.ZOOM_SUB_DARK : AIIcons.ZOOM_SUB_LIGHT);
        this.browser = browser;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        browser.setZoomLevel(browser.getZoomLevel() - 0.1D);
    }
}
