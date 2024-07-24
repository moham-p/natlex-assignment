package com.natlex.assignment.api.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Builder;

@Builder
public record SectionRequest(
    @NotBlank(message = "Name is mandatory") String name,
    @NotNull(message = "Geological classes cannot be null") @Valid
        List<GeologicalClassRequest> geologicalClasses) {}
