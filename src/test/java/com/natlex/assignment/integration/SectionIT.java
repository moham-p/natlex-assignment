package com.natlex.assignment.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.natlex.assignment.api.request.GeologicalClassRequest;
import com.natlex.assignment.api.request.SectionRequest;
import com.natlex.assignment.model.GeologicalClass;
import com.natlex.assignment.model.Section;
import com.natlex.assignment.persistence.GeologicalClassRepository;
import com.natlex.assignment.persistence.SectionRepository;

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SectionIT {

  @Autowired private SectionRepository sectionRepository;

  @Autowired private GeologicalClassRepository geologicalClassRepository;

  @Autowired WebTestClient testClient;

  private final String url = "/api/v1/sections";

  @BeforeEach
  void setUp() {
    sectionRepository.deleteAll();
    geologicalClassRepository.deleteAll();
  }

  @Test
  void createSection() {

    var requestBody =
        SectionRequest.builder()
            .name("Section 1")
            .geologicalClasses(
                List.of(
                    GeologicalClassRequest.builder().name("Geo Class 11").code("GC11").build(),
                    GeologicalClassRequest.builder().name("Geo Class 12").code("GC12").build()))
            .build();

    testClient
        .post()
        .uri(url)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(requestBody)
        .accept(MediaType.APPLICATION_JSON)
        .headers(headers -> headers.setBasicAuth("user", "password"))
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody()
        .jsonPath("$.name")
        .isEqualTo(requestBody.name());

    assertEquals(1, sectionRepository.findAll().size());
    assertEquals(2, geologicalClassRepository.findAll().size());
  }

  @Test
  void getAllSections() {

    sectionRepository.save(
        Section.builder().name("name1").geologicalClasses(Collections.emptyList()).build());
    sectionRepository.save(
        Section.builder().name("name2").geologicalClasses(Collections.emptyList()).build());

    testClient
        .get()
        .uri(url)
        .accept(MediaType.APPLICATION_JSON)
        .headers(headers -> headers.setBasicAuth("user", "password"))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.length()")
        .isEqualTo(2)
        .jsonPath("$.[0].name")
        .isEqualTo("name1")
        .jsonPath("$.[1].name")
        .isEqualTo("name2");
  }

  @Test
  void getSectionById() {
    var savedSection =
        sectionRepository.save(
            Section.builder().name("name").geologicalClasses(Collections.emptyList()).build());

    testClient
        .get()
        .uri(uriBuilder -> uriBuilder.path(url + "/" + savedSection.getId()).build())
        .accept(MediaType.APPLICATION_JSON)
        .headers(headers -> headers.setBasicAuth("user", "password"))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.name")
        .isEqualTo(savedSection.getName());
  }

  @Test
  void updateSection() {
    var savedSection =
        sectionRepository.save(
            Section.builder().name("name").geologicalClasses(Collections.emptyList()).build());
    var updatedSection =
        SectionRequest.builder()
            .name("updatedName")
            .geologicalClasses(Collections.emptyList())
            .build();

    testClient
        .put()
        .uri(uriBuilder -> uriBuilder.path(url + "/" + savedSection.getId()).build())
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(updatedSection)
        .accept(MediaType.APPLICATION_JSON)
        .headers(headers -> headers.setBasicAuth("user", "password"))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.name")
        .isEqualTo(updatedSection.name());
  }

  @Test
  void deleteSection() {
    var savedSection =
        sectionRepository.save(
            Section.builder().name("name").geologicalClasses(Collections.emptyList()).build());

    testClient
        .delete()
        .uri(uriBuilder -> uriBuilder.path(url + "/" + savedSection.getId()).build())
        .headers(headers -> headers.setBasicAuth("user", "password"))
        .exchange()
        .expectStatus()
        .isNoContent();

    assertEquals(0, sectionRepository.findAll().size());
  }

  @Test
  void getSectionsByGeologicalClassCode() {

    var savedSection1 = sectionRepository.save(Section.builder().name("name1").build());
    var savedSection2 = sectionRepository.save(Section.builder().name("name2").build());
    var savedSection3 = sectionRepository.save(Section.builder().name("name3").build());

    geologicalClassRepository.save(
        GeologicalClass.builder().name("name1").code("code1").section(savedSection1).build());
    geologicalClassRepository.save(
        GeologicalClass.builder().name("name2").code("code2").section(savedSection1).build());

    geologicalClassRepository.save(
        GeologicalClass.builder().name("name2").code("code2").section(savedSection2).build());

    geologicalClassRepository.save(
        GeologicalClass.builder().name("name3").code("code3").section(savedSection3).build());

    testClient
        .get()
        .uri(uriBuilder -> uriBuilder.path(url + "/by-code").queryParam("code", "code2").build())
        .accept(MediaType.APPLICATION_JSON)
        .headers(headers -> headers.setBasicAuth("user", "password"))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.length()")
        .isEqualTo(2)
        .jsonPath("$.[0].name")
        .isEqualTo("name1")
        .jsonPath("$.[1].name")
        .isEqualTo("name2");
  }
}
