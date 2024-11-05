package com.ai.topic;

import com.intellij.util.messages.Topic;
import com.ai.chat.AssistantType;


/**
 * 切换环境事件通知
 **/
public interface OpenAITopic {
    @Topic.ProjectLevel
    Topic<OpenAITopic> TOPIC = Topic.create(OpenAITopic.class.getName(), OpenAITopic.class);

    void change(AssistantType type);
}
