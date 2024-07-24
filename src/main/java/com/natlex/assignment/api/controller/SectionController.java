package com.natlex.assignment.api.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.natlex.assignment.api.request.SectionRequest;
import com.natlex.assignment.api.response.SectionResponse;
import com.natlex.assignment.service.SectionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/sections")
@RequiredArgsConstructor
public class SectionController {

  private final SectionService sectionService;

  @PostMapping
  public ResponseEntity<SectionResponse> createSection(@Valid @RequestBody SectionRequest request) {
    SectionResponse response = sectionService.saveSection(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping
  public ResponseEntity<List<SectionResponse>> getAllSections() {
    List<SectionResponse> sectionResponses = sectionService.getAllSections();
    return ResponseEntity.ok().body(sectionResponses);
  }

  @GetMapping("/{id}")
  public ResponseEntity<SectionResponse> getSectionById(@PathVariable Long id) {
    SectionResponse sectionResponse = sectionService.getSectionById(id);
    return ResponseEntity.ok().body(sectionResponse);
  }

  @PutMapping("/{id}")
  public ResponseEntity<SectionResponse> updateSection(
      @PathVariable Long id, @Valid @RequestBody SectionRequest request) {
    SectionResponse sectionResponse = sectionService.updateSection(id, request);
    return ResponseEntity.ok().body(sectionResponse);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteSection(@PathVariable Long id) {
    sectionService.deleteSection(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/by-code")
  public ResponseEntity<List<SectionResponse>> getSectionsByGeologicalClassCode(
      @RequestParam String code) {
    List<SectionResponse> sectionResponses = sectionService.getSectionsByGeologicalClassCode(code);
    return ResponseEntity.ok().body(sectionResponses);
  }
}
