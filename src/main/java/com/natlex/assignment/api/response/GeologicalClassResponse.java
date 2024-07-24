package com.natlex.assignment.api.response;

import lombok.Builder;

@Builder
public record GeologicalClassResponse(Long id, String name, String code, Long sectionId) {}
