
package com.ai.gui;

import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;

import javax.swing.*;

public class GUIKit {

    public static void showCallout(JComponent component, String content, MessageType messageType) {
        JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder(content, messageType, null)
                .setFadeoutTime((messageType == MessageType.ERROR) ? 0 : 5000)
                .createBalloon()
                .show(RelativePoint.getSouthOf(component), Balloon.Position.below);
    }

}
