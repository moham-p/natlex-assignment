package com.natlex.assignment.api.request;

import jakarta.validation.constraints.NotBlank;

import lombok.Builder;

@Builder
public record GeologicalClassRequest(
    @NotBlank(message = "Name is mandatory") String name,
    @NotBlank(message = "Code is mandatory") String code) {}
