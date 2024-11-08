package com.ai.gui.prompt.context;

import javax.swing.*;

public class ListSeparator {

    private final String myText;
    private final Icon myIcon;

    public ListSeparator() {
        this("");
    }

    public ListSeparator(String aText) {
        this(aText, null);
    }

    public ListSeparator(String name, Icon icon) {
        myText = name;
        myIcon = icon;
    }

    public String getText() {
        return myText;
    }

    public Icon getIcon() {
        return myIcon;
    }
}
