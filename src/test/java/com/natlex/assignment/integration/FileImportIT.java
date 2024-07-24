package com.natlex.assignment.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import com.natlex.assignment.model.Job;
import com.natlex.assignment.model.JobState;
import com.natlex.assignment.model.JobType;
import com.natlex.assignment.persistence.JobRepository;

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FileImportIT {

  @Autowired private WebTestClient testClient;

  @Autowired private JobRepository jobRepository;

  private final String url = "/api/v1/import";

  @BeforeEach
  void setUp() {
    jobRepository.deleteAll();
  }

  @Test
  void importFile_shouldReturnJobIdWith201() throws IOException {

    MultipartBodyBuilder bodyBuilder = createBodyBuilder("test-import-file.xls");

    testClient
        .post()
        .uri(url)
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
        .accept(MediaType.APPLICATION_JSON)
        .headers(headers -> headers.setBasicAuth("admin", "adminpassword"))
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody()
        .jsonPath("$.id")
        .exists();

    assertEquals(1, jobRepository.findAll().size());
  }

  private MultipartBodyBuilder createBodyBuilder(String fileName) throws IOException {

    MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
    bodyBuilder
        .part("file", new ClassPathResource(fileName).getFile(), MediaType.APPLICATION_JSON)
        .header("Content-Disposition", "form-data; name=\"file\"; filename=" + fileName)
        .header("Content-Type", "application/json");

    return bodyBuilder;
  }

  @Test
  void getImportJobState() {

    var job = Job.builder().id("jobId123").jobType(JobType.IMPORT).jobState(JobState.DONE).build();
    jobRepository.save(job);

    testClient
        .get()
        .uri(url + "/" + job.getId())
        .accept(MediaType.APPLICATION_JSON)
        .headers(headers -> headers.setBasicAuth("admin", "adminpassword"))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.state")
        .isEqualTo(JobState.DONE.name());
  }
}
