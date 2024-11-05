package com.ai.gui.browser.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.jcef.JBCefBrowser;
import com.ai.util.ThemeTool;
import com.ai.icons.AIIcons;
import org.jetbrains.annotations.NotNull;


public class RefreshPage extends AnAction {

    private final JBCefBrowser browser;

    public RefreshPage(JBCefBrowser browser) {
        super(() -> "RefreshPage", ThemeTool.isDark() ? AIIcons.RELOAD_DARK : AIIcons.RELOAD_LIGHT);
        this.browser = browser;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        browser.getCefBrowser().reload();
    }
}
