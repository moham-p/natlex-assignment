package com.natlex.assignment.integration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.natlex.assignment.api.request.GeologicalClassRequest;
import com.natlex.assignment.model.GeologicalClass;
import com.natlex.assignment.model.Section;
import com.natlex.assignment.persistence.GeologicalClassRepository;
import com.natlex.assignment.persistence.SectionRepository;

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GeologicalClassIT {

  @Autowired private GeologicalClassRepository geologicalClassRepository;

  @Autowired private SectionRepository sectionRepository;

  @Autowired WebTestClient testClient;

  private final String url = "/api/v1/geologicalClasses";

  @BeforeEach
  void setUp() {
    geologicalClassRepository.deleteAll();
    sectionRepository.deleteAll();
  }

  @Test
  void createGeologicalClass() {

    Section savedSection = sectionRepository.save(Section.builder().name("name").build());

    var requestBody = GeologicalClassRequest.builder().name("name").code("code").build();

    testClient
        .post()
        .uri(
            uriBuilder ->
                uriBuilder.path(url).queryParam("sectionId", savedSection.getId()).build())
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(requestBody)
        .accept(MediaType.APPLICATION_JSON)
        .headers(headers -> headers.setBasicAuth("user", "password"))
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody()
        .jsonPath("$.name")
        .isEqualTo(requestBody.name())
        .jsonPath("$.code")
        .isEqualTo(requestBody.code());

    assertEquals(1, geologicalClassRepository.findAll().size());
  }

  @Test
  void getAllGeologicalClasses() {

    Section section = Section.builder().name("name").build();
    sectionRepository.save(section);

    geologicalClassRepository.save(
        GeologicalClass.builder().name("n1").code("c1").section(section).build());
    geologicalClassRepository.save(
        GeologicalClass.builder().name("n2").code("c2").section(section).build());

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
        .isEqualTo("n1")
        .jsonPath("$.[0].code")
        .isEqualTo("c1")
        .jsonPath("$.[1].name")
        .isEqualTo("n2")
        .jsonPath("$.[1].code")
        .isEqualTo("c2");
  }

  @Test
  void getGeologicalClassById() {

    var savedSection = sectionRepository.save(Section.builder().name("name").build());
    var savedGeologicalClass =
        geologicalClassRepository.save(
            GeologicalClass.builder().name("name").code("code").section(savedSection).build());

    testClient
        .get()
        .uri(uriBuilder -> uriBuilder.path(url + "/" + savedGeologicalClass.getId()).build())
        .accept(MediaType.APPLICATION_JSON)
        .headers(headers -> headers.setBasicAuth("user", "password"))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.name")
        .isEqualTo(savedGeologicalClass.getName())
        .jsonPath("$.code")
        .isEqualTo(savedGeologicalClass.getCode());
  }

  @Test
  void updateGeologicalClass() {
    var savedSection = sectionRepository.save(Section.builder().name("name").build());
    var savedGeologicalClass =
        geologicalClassRepository.save(
            GeologicalClass.builder().name("name").code("code").section(savedSection).build());

    var updatedGeologicalClassRequest =
        GeologicalClassRequest.builder().name("updated_name").code("updated_code").build();

    testClient
        .put()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path(url + "/" + savedGeologicalClass.getId())
                    .queryParam("sectionId", savedSection.getId())
                    .build())
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(updatedGeologicalClassRequest)
        .accept(MediaType.APPLICATION_JSON)
        .headers(headers -> headers.setBasicAuth("user", "password"))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.name")
        .isEqualTo(updatedGeologicalClassRequest.name())
        .jsonPath("$.code")
        .isEqualTo(updatedGeologicalClassRequest.code());
  }

  @Test
  void deleteGeologicalClass_shouldReturn204() {

    var savedSection = sectionRepository.save(Section.builder().name("name").build());
    var savedGeologicalClass =
        geologicalClassRepository.save(
            GeologicalClass.builder().name("name").code("code").section(savedSection).build());

    testClient
        .delete()
        .uri(uriBuilder -> uriBuilder.path(url + "/" + savedGeologicalClass.getId()).build())
        .headers(headers -> headers.setBasicAuth("user", "password"))
        .exchange()
        .expectStatus()
        .isNoContent();

    assertEquals(0, geologicalClassRepository.findAll().size());
  }
}
