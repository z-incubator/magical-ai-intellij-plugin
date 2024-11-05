
package com.ai.chat.client;

import com.intellij.openapi.diagnostic.Logger;
import com.ai.chat.ChatMessageEvent;
import com.ai.chat.ChatMessageListener;
import com.ai.chat.ConversationContext;
import com.ai.chat.models.ModelType;
import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Subscription;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.Consumer;

public class ChatHandler {

    private static final Logger LOG = Logger.getInstance(ChatHandler.class);

    public Flux<?> handle(ConversationContext ctx, ChatMessageEvent.Initiating event, ChatMessageListener listener) {
        var modelType = ctx.getModelType();
        var chatClient = ChatClientHolder.getChatClient(ctx.getAssistantType());
        var flowHandler = new ChatCompletionHandler(listener);
        var prompt = event.getPrompt()
                .map(prmpt -> maybeOverrideChatOptions(modelType, prmpt))
                .orElseThrow(() -> new IllegalArgumentException("Prompt is required"));

        if (modelType.supportsStreaming()) {
            try {
                return chatClient.prompt(prompt).stream().chatResponse()
                        .doOnSubscribe(flowHandler.onSubscribe(event))
                        .doOnError(flowHandler.onError())
                        .doOnComplete(flowHandler.onComplete(ctx))
                        .doOnNext(flowHandler.onNextChunk());
            } catch (UnsupportedOperationException ignore) {
                // fall through
            }
        }
        return Mono.fromCallable(() -> chatClient.prompt(prompt).call().chatResponse())
                .flux()
                .doOnSubscribe(flowHandler.onSubscribe(event))
                .doOnError(flowHandler.onError())
                .doOnComplete(flowHandler.onComplete(ctx))
                .doOnNext(flowHandler.onNext());
    }

    private Prompt maybeOverrideChatOptions(ModelType modelType, Prompt prompt) {
        var optionsOverride = modelType.incompatibleChatOptionsOverride();
        if (optionsOverride != ModelType.OVERRIDE_NONE)
            prompt = new Prompt(prompt.getInstructions(), optionsOverride);

        return prompt;
    }

    static class ChatCompletionHandler {
        private final ChatMessageListener listener;
        private final SortedMap<Integer, StringBuffer> partialResponseChoices;
        private final SortedMap<Integer, ChatResponseMetadata> lastMetadata;
        private volatile ChatMessageEvent.Started event;

        public ChatCompletionHandler(ChatMessageListener listener) {
            this.listener = listener;
            this.partialResponseChoices = Collections.synchronizedSortedMap(new TreeMap<>());
            this.lastMetadata = Collections.synchronizedSortedMap(new TreeMap<>());
        }

        public Consumer<Subscription> onSubscribe(ChatMessageEvent.Initiating event) {
            return subscription -> listener.exchangeStarted(this.event = event.started(subscription));
        }

        public Runnable onComplete(ConversationContext ctx) {
            return () -> {
                var assistantMessages = toMessages(partialResponseChoices);
                if (!assistantMessages.isEmpty()) {
                    ctx.addChatMessage(assistantMessages.get(0).getOutput());
                }
                listener.responseArrived(event.responseArrived(new ChatResponse(assistantMessages, lastMetadata.get(0))));
            };
        }

        public Consumer<ChatResponse> onNextChunk() {
            return chunk -> {
                if (chunk.getResult() != null) {
                    listener.responseArriving(event.responseArriving(chunk, formResponse(chunk, chunk.getResult())));
                } else if (chunk.getMetadata() != null) {
                    lastMetadata.put(0, chunk.getMetadata());
                }
            };
        }

        public Consumer<ChatResponse> onNext() {
            return result -> {
                if (result.getResult() != null) {
                    listener.responseArrived(event.responseArrived(new ChatResponse(formResponse(result, result.getResult()), lastMetadata.get(0))));
                }
            };
        }

        public Consumer<Throwable> onError() {
            return cause -> {
                listener.exchangeFailed(event.failed(cause));
                LOG.error(cause.getMessage());
            };
        }

        private List<Generation> formResponse(ChatResponse response, Generation choice) {
            partialResponseChoices.computeIfAbsent(0, __ -> new StringBuffer())
                    .append(StringUtils.defaultIfEmpty(choice.getOutput().getContent(), ""));
            lastMetadata.put(0, response.getMetadata());
            return toMessages(partialResponseChoices);
        }

        private List<Generation> toMessages(SortedMap<Integer, StringBuffer> partialResponseChoices) {
            List<AssistantMessage> responseChoices = new ArrayList<>(partialResponseChoices.size());

            // 将 StringBuffer 转换为 AssistantMessage 并添加到 responseChoices 列表
            partialResponseChoices.forEach((key, value) -> responseChoices.add(new AssistantMessage(value.toString())));

            // 假设 Generation 需要传入 AssistantMessage，而不是 String
            return List.of(new Generation(responseChoices.get(0)));
        }

    }
}
