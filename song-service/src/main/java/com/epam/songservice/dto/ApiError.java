package com.epam.songservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        String errorMessage,
        Map<String, String> details,
        String errorCode
) {
}
