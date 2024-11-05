
package com.ai.gui;

import com.ai.text.CodeFragment;
import com.intellij.openapi.project.Project;

import java.util.List;

public interface ContextAwareAppetizer {

    List<CodeFragment> fetchSnippets(Project project);

}
