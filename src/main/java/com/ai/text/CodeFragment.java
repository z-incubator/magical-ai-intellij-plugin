
package com.ai.text;

import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

/**
 * A fragment of executable code, along with metadata such as the language and a brief description of the code.
 * It extends the {@code CharSequence} interface to enable basic string manipulation methods, as well as
 * includes a set of factory methods that create instances of a Java record that implements the
 * {@code CodeFragment} interface.
 */
public interface CodeFragment extends CharSequence, TextContent {

    String language();

    String content();

    String description();

    static CodeFragment of(String content) {
        return of(content, "", "");
    }

    static CodeFragment of(String content, String language) {
        return of(content, language, "");
    }

    static CodeFragment of(String content, String language, String description) {
        return new Of(content, language, description);
    }

    record Of(String content, String language, String description) implements CodeFragment {
        public Of {
            requireNonNull(content, "content");
            requireNonNull(language, "language");
            requireNonNull(description, "description");
        }

        @Override
        public @NotNull String toString() {
            return content;
        }
    }

    @Override
    default int length() {
        return content().length();
    }

    @Override
    default char charAt(int index) {
        return content().charAt(index);
    }

    @Override
    default @NotNull CharSequence subSequence(int start, int end) {
        return content().subSequence(start, end);
    }

    default String toMarkdownString() {
        return CodeFragmentMarkdownFormatter.getDefault().format(this);
    }

    default StringBuilder appendTo(StringBuilder buf) {
        return buf.append(toMarkdownString());
    }
}
