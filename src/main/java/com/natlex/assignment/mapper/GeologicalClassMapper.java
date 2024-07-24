package com.natlex.assignment.mapper;

import com.natlex.assignment.api.request.GeologicalClassRequest;
import com.natlex.assignment.api.response.GeologicalClassResponse;
import com.natlex.assignment.model.GeologicalClass;

public class GeologicalClassMapper {

  public static GeologicalClassRequest toRequest(GeologicalClass geologicalClass) {
    if (geologicalClass == null) {
      return null;
    }

    return GeologicalClassRequest.builder()
        .name(geologicalClass.getName())
        .code(geologicalClass.getCode())
        .build();
  }

  public static GeologicalClassResponse toResponse(GeologicalClass geologicalClass) {
    if (geologicalClass == null) {
      return null;
    }

    return GeologicalClassResponse.builder()
        .id(geologicalClass.getId())
        .name(geologicalClass.getName())
        .code(geologicalClass.getCode())
        .sectionId(geologicalClass.getSection().getId())
        .build();
  }

  public static GeologicalClass toEntity(GeologicalClassRequest request) {
    if (request == null) {
      return null;
    }

    return GeologicalClass.builder().name(request.name()).code(request.code()).build();
  }
}
