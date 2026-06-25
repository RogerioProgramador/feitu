package com.feitu.dto;

import jakarta.validation.constraints.NotBlank;

public record SubscribeRequest(
        @NotBlank String endpoint,
        Keys keys
) {
    public record Keys(@NotBlank String p256dh, @NotBlank String auth) {}
}
