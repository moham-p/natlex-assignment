package com.natlex.assignment.service;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.natlex.assignment.api.request.GeologicalClassRequest;
import com.natlex.assignment.api.response.GeologicalClassResponse;
import com.natlex.assignment.mapper.GeologicalClassMapper;
import com.natlex.assignment.model.GeologicalClass;
import com.natlex.assignment.model.Section;
import com.natlex.assignment.persistence.GeologicalClassRepository;
import com.natlex.assignment.persistence.SectionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GeologicalClassService {

  private final GeologicalClassRepository geologicalClassRepository;
  private final SectionRepository sectionRepository;

  @Transactional
  public GeologicalClassResponse saveGeologicalClass(
      GeologicalClassRequest request, Long sectionId) {
    GeologicalClass geologicalClass = GeologicalClassMapper.toEntity(request);
    Section section =
        sectionRepository
            .findById(sectionId)
            .orElseThrow(() -> new EntityNotFoundException("Section not found"));
    geologicalClass.setSection(section);
    GeologicalClass savedGeologicalClass = geologicalClassRepository.save(geologicalClass);
    return GeologicalClassMapper.toResponse(savedGeologicalClass);
  }

  @Transactional
  public GeologicalClassResponse updateGeologicalClass(Long id, GeologicalClassRequest request) {
    GeologicalClass geologicalClass =
        geologicalClassRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Geological Class not found"));
    geologicalClass.setName(request.name());
    geologicalClass.setCode(request.code());
    GeologicalClass updatedGeologicalClass = geologicalClassRepository.save(geologicalClass);
    return GeologicalClassMapper.toResponse(updatedGeologicalClass);
  }

  public GeologicalClassResponse getGeologicalClassById(Long id) {
    GeologicalClass geologicalClass =
        geologicalClassRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Geological Class not found"));
    return GeologicalClassMapper.toResponse(geologicalClass);
  }

  public List<GeologicalClassResponse> getAllGeologicalClasses() {
    return geologicalClassRepository.findAll().stream()
        .map(GeologicalClassMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Transactional
  public void deleteGeologicalClass(Long id) {
    geologicalClassRepository.deleteById(id);
  }
}
