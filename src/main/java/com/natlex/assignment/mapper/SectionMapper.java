package com.natlex.assignment.mapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.natlex.assignment.api.request.SectionRequest;
import com.natlex.assignment.api.response.SectionResponse;
import com.natlex.assignment.model.GeologicalClass;
import com.natlex.assignment.model.Section;

public class SectionMapper {

  public static SectionRequest toRequest(Section section) {
    return Optional.ofNullable(section)
        .map(
            sec ->
                SectionRequest.builder()
                    .name(sec.getName())
                    .geologicalClasses(
                        Optional.ofNullable(sec.getGeologicalClasses())
                            .orElse(Collections.emptyList())
                            .stream()
                            .map(GeologicalClassMapper::toRequest)
                            .collect(Collectors.toList()))
                    .build())
        .orElse(null);
  }

  public static SectionResponse toResponse(Section section) {
    return Optional.ofNullable(section)
        .map(
            sec ->
                SectionResponse.builder()
                    .id(sec.getId())
                    .name(sec.getName())
                    .geologicalClasses(
                        Optional.ofNullable(sec.getGeologicalClasses())
                            .orElse(Collections.emptyList())
                            .stream()
                            .map(GeologicalClassMapper::toRequest)
                            .collect(Collectors.toList()))
                    .build())
        .orElse(null);
  }

  public static Section toEntity(SectionRequest request) {
    return Optional.ofNullable(request)
        .map(
            req -> {
              Section section = Section.builder().name(req.name()).build();

              List<GeologicalClass> geologicalClasses =
                  Optional.ofNullable(req.geologicalClasses())
                      .orElse(Collections.emptyList())
                      .stream()
                      .map(
                          geoReq -> {
                            GeologicalClass geoClass = GeologicalClassMapper.toEntity(geoReq);
                            geoClass.setSection(section);
                            return geoClass;
                          })
                      .collect(Collectors.toList());

              section.setGeologicalClasses(geologicalClasses);
              return section;
            })
        .orElse(null);
  }
}
