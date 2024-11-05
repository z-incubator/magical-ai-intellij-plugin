
package com.ai.gui.prompt.context;

public interface SpeedSearchFilter<T> {
  default boolean canBeHidden(T value) {
    return true;
  }

  String getIndexedString(T value);
}
