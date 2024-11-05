package com.ai.core;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public final class Errors {

    private static final int MESSAGE_MAX_LENGTH = 1000;


    public static String getWebClientErrorMessage(Throwable cause) {
        String errorMessage = getErrorMessage(cause);
        return errorMessage + (errorMessage.isEmpty() ? "" : "\n\n") + getErrorResponseBody(cause);
    }

    private static String getErrorResponseBody(Throwable cause) {
        var restEx = (cause instanceof WebClientResponseException were) ? were
                : (cause.getCause() instanceof WebClientResponseException were) ? were : null;
        return (restEx != null) ? StringUtils.abbreviate(restEx.getResponseBodyAsString(), MESSAGE_MAX_LENGTH) : "";
    }

    private static String getErrorMessage(Throwable cause) {
        if (cause == null)
            return "";

        var errorMessage = isEmpty(cause.getMessage()) ? "" : StringUtils.abbreviate(cause.getMessage(), MESSAGE_MAX_LENGTH);
        var causeMessage = getErrorMessage(cause.getCause());
        if (errorMessage.isEmpty() || causeMessage.contains(errorMessage))
            return causeMessage;
        else
            return errorMessage;
    }

}
