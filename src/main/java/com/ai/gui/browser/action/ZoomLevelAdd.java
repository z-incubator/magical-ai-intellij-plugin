package com.ai.gui.browser.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.jcef.JBCefBrowser;
import com.ai.util.ThemeTool;
import com.ai.icons.AIIcons;
import org.jetbrains.annotations.NotNull;


public class ZoomLevelAdd extends AnAction {

    private final JBCefBrowser browser;

    public ZoomLevelAdd(JBCefBrowser browser) {
        super(() -> "Zoom Level Increases", ThemeTool.isDark() ? AIIcons.ZOOM_ADD_DARK : AIIcons.ZOOM_ADD_LIGHT);
        this.browser = browser;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        browser.setZoomLevel(browser.getZoomLevel() + 0.1D);
    }
}
