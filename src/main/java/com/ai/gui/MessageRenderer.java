
package com.ai.gui;

import javax.swing.text.EditorKit;

public interface MessageRenderer {

    EditorKit getEditorKit();

    int getWidth();

    Object getClientProperty(Object key);
}
