package com.ai.gui.browser.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.ui.jcef.JBCefBrowser;
import com.ai.icons.AIIcons;
import com.ai.settings.state.GeneralSettings;
import com.ai.util.ThemeTool;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;



public class ClearCookies extends AnAction {

    private final JBCefBrowser browser;
    private final JPanel contentPanel;

    public ClearCookies(JBCefBrowser browser, JPanel contentPanel) {
        super(() -> "Clear Cookies", ThemeTool.isDark() ? AIIcons.CLEAR_COOKIE_DARK : AIIcons.CLEAR_COOKIE_LIGHT);
        this.browser = browser;
        this.contentPanel = contentPanel;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        boolean yes = MessageDialogBuilder.yesNo("Are you sure you want to clear?",
                        "Once the cookies are cleared, you will need to " +
                                "login again, are you sure to continue?")
                .yesText("Yes")
                .noText("No").ask(contentPanel);
        if (yes) {
            browser.getJBCefCookieManager().getCefCookieManager().deleteCookies(GeneralSettings.getInstance().getOnlineUrl(), "");
        }
    }
}
