
package com.ai.text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeSnippetManipulator {

    private static final Pattern CODE_SNIPPET_BLOCK_PATTERN = Pattern.compile("(\\[(.*?)](?:<br>)?(?:\\s*(`{3,}).*?\\3)+)(?:<br>|\\s)*", Pattern.DOTALL);

    public static String makeCodeSnippetBlocksCollapsible(String input) {
        Matcher matcher = CODE_SNIPPET_BLOCK_PATTERN.matcher(input);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            if (matcher.groupCount() < 2) {
                continue;
            }
            String fullMatch = matcher.group(1);
            String title = matcher.group(2);

            // Replace with the new div tag
            title = title.replace("\\", "\\\\").replace("$", "\\$");
            fullMatch = fullMatch.replace("\\", "\\\\").replace("$", "\\$");
            matcher.appendReplacement(sb, "<div class=\"collapsible\" ai-code-snippet title=\"" + title + "\">\n" + fullMatch + "\n</div>");
        }
        matcher.appendTail(sb);

        return sb.toString();
    }
}
