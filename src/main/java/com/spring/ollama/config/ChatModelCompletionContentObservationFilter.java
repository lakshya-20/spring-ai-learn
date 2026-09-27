package com.spring.ollama.config;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationFilter;
import org.springframework.ai.chat.observation.ChatModelObservationContext;
import org.springframework.ai.content.Content;
import org.springframework.ai.observation.ObservabilityHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Adds gen_ai.prompt / gen_ai.completion as high-cardinality span attributes so
 * Langfuse can populate generation input/output. Spring AI's log-prompt /
 * log-completion flags only keep the data in the observation context; this
 * filter exports it onto the OpenTelemetry span.
 */
@Component
public class ChatModelCompletionContentObservationFilter implements ObservationFilter {

    @Override
    public Observation.Context map(Observation.Context context) {
        if (!(context instanceof ChatModelObservationContext chatModelObservationContext)) {
            return context;
        }

        List<String> prompts = processPrompts(chatModelObservationContext);
        List<String> completions = processCompletion(chatModelObservationContext);

        chatModelObservationContext.addHighCardinalityKeyValue(KeyValue.of(
                "gen_ai.prompt",
                ObservabilityHelper.concatenateStrings(prompts)));

        chatModelObservationContext.addHighCardinalityKeyValue(KeyValue.of(
                "gen_ai.completion",
                ObservabilityHelper.concatenateStrings(completions)));

        return chatModelObservationContext;
    }

    private List<String> processPrompts(ChatModelObservationContext context) {
        if (CollectionUtils.isEmpty(context.getRequest().getInstructions())) {
            return List.of();
        }
        return context.getRequest().getInstructions().stream()
                .map(Content::getText)
                .toList();
    }

    private List<String> processCompletion(ChatModelObservationContext context) {
        if (context.getResponse() == null
                || context.getResponse().getResults() == null
                || CollectionUtils.isEmpty(context.getResponse().getResults())) {
            return List.of();
        }
        if (!StringUtils.hasText(context.getResponse().getResult().getOutput().getText())) {
            return List.of();
        }
        return context.getResponse().getResults().stream()
                .filter(generation -> generation.getOutput() != null
                        && StringUtils.hasText(generation.getOutput().getText()))
                .map(generation -> generation.getOutput().getText())
                .toList();
    }

}