package com.natlex.assignment.api.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.natlex.assignment.api.request.GeologicalClassRequest;
import com.natlex.assignment.api.response.GeologicalClassResponse;
import com.natlex.assignment.service.GeologicalClassService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/geologicalClasses")
@RequiredArgsConstructor
public class GeologicalClassController {

  private final GeologicalClassService geologicalClassService;

  @PostMapping
  public ResponseEntity<GeologicalClassResponse> createGeologicalClass(
      @Valid @RequestBody GeologicalClassRequest request, @RequestParam Long sectionId) {
    GeologicalClassResponse response =
        geologicalClassService.saveGeologicalClass(request, sectionId);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping
  public ResponseEntity<List<GeologicalClassResponse>> getAllGeologicalClasses() {
    List<GeologicalClassResponse> geologicalClassResponses =
        geologicalClassService.getAllGeologicalClasses();
    return ResponseEntity.ok().body(geologicalClassResponses);
  }

  @GetMapping("/{id}")
  public ResponseEntity<GeologicalClassResponse> getGeologicalClassById(@PathVariable Long id) {
    GeologicalClassResponse geologicalClassResponse =
        geologicalClassService.getGeologicalClassById(id);
    return ResponseEntity.ok().body(geologicalClassResponse);
  }

  @PutMapping("/{id}")
  public ResponseEntity<GeologicalClassResponse> updateGeologicalClass(
      @PathVariable Long id, @RequestBody @Valid GeologicalClassRequest request) {
    GeologicalClassResponse geologicalClassResponse =
        geologicalClassService.updateGeologicalClass(id, request);
    return ResponseEntity.ok().body(geologicalClassResponse);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteGeologicalClass(@PathVariable Long id) {
    geologicalClassService.deleteGeologicalClass(id);
    return ResponseEntity.noContent().build();
  }
}
