package com.ai.topic;

import com.intellij.util.messages.Topic;


/**
 * 切换环境事件通知
 **/
public interface FileTopic {
    @Topic.ProjectLevel
    Topic<FileTopic> TOPIC = Topic.create(FileTopic.class.getName(), FileTopic.class);

    void fileChange();
}
