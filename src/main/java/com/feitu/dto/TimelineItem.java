package com.feitu.dto;

import java.time.Instant;
import java.util.UUID;

public record TimelineItem(UUID tarefaId, String tarefaNome, String workspaceCor, Instant inicio, Instant fim) {}
