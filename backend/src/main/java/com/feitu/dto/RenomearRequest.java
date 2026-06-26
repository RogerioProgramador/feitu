package com.feitu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RenomearRequest(@NotBlank @Size(max = 200) String nome) {}
