package com.natlex.assignment.service;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.natlex.assignment.api.request.SectionRequest;
import com.natlex.assignment.api.response.SectionResponse;
import com.natlex.assignment.mapper.GeologicalClassMapper;
import com.natlex.assignment.mapper.SectionMapper;
import com.natlex.assignment.model.GeologicalClass;
import com.natlex.assignment.model.Section;
import com.natlex.assignment.persistence.SectionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SectionService {

  private final SectionRepository sectionRepository;

  @Transactional
  public SectionResponse saveSection(SectionRequest request) {
    Section section = SectionMapper.toEntity(request);
    Section savedSection = sectionRepository.save(section);
    return SectionMapper.toResponse(savedSection);
  }

  @Transactional
  public void saveImportedSection(SectionRequest request, String JobId) {
    Section section = SectionMapper.toEntity(request);
    section.setJobId(JobId);
    sectionRepository.save(section);
  }

  @Transactional
  public SectionResponse updateSection(Long id, SectionRequest request) {
    Section existingSection =
        sectionRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Section not found"));

    existingSection.getGeologicalClasses().clear();

    List<GeologicalClass> updatedGeologicalClasses =
        request.geologicalClasses().stream()
            .map(
                req -> {
                  GeologicalClass geoClass = GeologicalClassMapper.toEntity(req);
                  geoClass.setSection(existingSection);
                  return geoClass;
                })
            .collect(Collectors.toList());

    existingSection.getGeologicalClasses().addAll(updatedGeologicalClasses);
    existingSection.setName(request.name());

    Section updatedSection = sectionRepository.save(existingSection);
    return SectionMapper.toResponse(updatedSection);
  }

  @Transactional
  public List<SectionResponse> getAllSections() {
    return sectionRepository.findAll().stream()
        .map(SectionMapper::toResponse)
        .collect(Collectors.toList());
  }

  public SectionResponse getSectionById(Long id) {
    Section section =
        sectionRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Section not found"));
    return SectionMapper.toResponse(section);
  }

  @Transactional
  public void deleteSection(Long id) {
    sectionRepository.deleteById(id);
  }

  public List<SectionResponse> getSectionsByGeologicalClassCode(String code) {
    List<Section> sections = sectionRepository.findByGeologicalClassCode(code);
    return sections.stream().map(SectionMapper::toResponse).collect(Collectors.toList());
  }
}
