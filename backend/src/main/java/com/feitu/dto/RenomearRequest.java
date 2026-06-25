package com.feitu.dto;

import jakarta.validation.constraints.NotBlank;

public record RenomearRequest(@NotBlank String nome) {}
