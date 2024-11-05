
package com.ai.text;

public interface CodeFragmentFormatter {

    String format(CodeFragment cf);

    CodeFragmentFormatter withoutDescription();
}
