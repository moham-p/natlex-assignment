package com.natlex.assignment.bootstrap;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.natlex.assignment.api.request.GeologicalClassRequest;
import com.natlex.assignment.api.request.SectionRequest;
import com.natlex.assignment.persistence.SectionRepository;
import com.natlex.assignment.service.SectionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializer implements CommandLineRunner {

  private final SectionRepository sectionRepository;
  private final SectionService sectionService;

  @Override
  public void run(String... args) throws Exception {

    if (sectionRepository.count() == 0) {

      SectionRequest section1 =
          SectionRequest.builder()
              .name("Init Section 1")
              .geologicalClasses(
                  List.of(
                      GeologicalClassRequest.builder()
                          .name("Init Geo Class 11")
                          .code("Init GC11")
                          .build(),
                      GeologicalClassRequest.builder()
                          .name("Init Geo Class 12")
                          .code("Init GC12")
                          .build()))
              .build();

      SectionRequest section2 =
          SectionRequest.builder()
              .name("Init Section 2")
              .geologicalClasses(
                  List.of(
                      GeologicalClassRequest.builder()
                          .name("Init Geo Class 21")
                          .code("Init GC21")
                          .build(),
                      GeologicalClassRequest.builder()
                          .name("Init Geo Class 22")
                          .code("Init GC22")
                          .build()))
              .build();

      sectionService.saveSection(section1);
      sectionService.saveSection(section2);

      log.info("Initialized database with sample data.");
    } else {
      log.info("Database already initialized.");
    }
  }
}
