package com.ai.text;

@FunctionalInterface
public interface TextContent {

    StringBuilder appendTo(StringBuilder a);

    static String toString(TextContent content) {
        return content.appendTo(new StringBuilder()).toString();
    }
}
