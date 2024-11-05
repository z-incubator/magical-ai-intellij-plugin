package com.ai.topic;

import com.intellij.util.messages.Topic;


/**
 * 切换环境事件通知
 **/
public interface OnlineTopic {
    @Topic.ProjectLevel
    Topic<OnlineTopic> TOPIC = Topic.create(OnlineTopic.class.getName(), OnlineTopic.class);

    void send(String type);
}
