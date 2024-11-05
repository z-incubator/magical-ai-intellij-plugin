
package com.ai.chat;

import java.util.List;

public interface InputContext {

    void addListener(InputContextListener listener);

    void removeListener(InputContextListener listener);

    void addAttachment(PromptAttachment attachment);

    void removeAttachment(PromptAttachment attachment);

    List<PromptAttachment> getAttachments();

    boolean isEmpty();

    void clear();

}
