
package com.ai.core;

@FunctionalInterface
public interface TextSubstitutor {
    TextSubstitutor NONE = (x -> x);

    String resolvePlaceholders(String text);

}
