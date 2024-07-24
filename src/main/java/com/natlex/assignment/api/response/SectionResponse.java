package com.natlex.assignment.api.response;

import java.util.List;

import com.natlex.assignment.api.request.GeologicalClassRequest;

import lombok.Builder;

@Builder
public record SectionResponse(
    Long id, String name, List<GeologicalClassRequest> geologicalClasses) {}
