package com.feitu.dto;

import jakarta.validation.constraints.NotBlank;

public record UnsubscribeRequest(@NotBlank String endpoint) {}
