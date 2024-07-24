package com.natlex.assignment.integration;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.natlex.assignment.model.Job;
import com.natlex.assignment.model.JobState;
import com.natlex.assignment.model.JobType;
import com.natlex.assignment.persistence.JobRepository;

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FileExportIT {

  @Autowired private WebTestClient testClient;

  @Autowired private JobRepository jobRepository;

  private final String url = "/api/v1/export";

  @Test
  void getExportJobStatus() {

    var job = Job.builder().id("jobId123").jobType(JobType.EXPORT).jobState(JobState.DONE).build();
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

  @Test
  void getExportedFile() throws IOException {

    var job =
        Job.builder()
            .id("jobId123")
            .jobType(JobType.EXPORT)
            .jobState(JobState.DONE)
            .filePath(new ClassPathResource("test-import-file.xls").getFile().getAbsolutePath())
            .build();
    jobRepository.save(job);

    testClient
        .get()
        .uri(url + "/" + job.getId() + "/file")
        .headers(headers -> headers.setBasicAuth("admin", "adminpassword"))
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .contentType("application/vnd.ms-excel")
        .expectBody(byte[].class)
        .returnResult()
        .getResponseBody();
  }

  @Test
  void getExportedFileWhileInProgress() throws IOException {

    var job =
        Job.builder().id("jobId123").jobType(JobType.EXPORT).jobState(JobState.IN_PROGRESS).build();
    jobRepository.save(job);

    testClient
        .get()
        .uri(url + "/" + job.getId() + "/file")
        .headers(headers -> headers.setBasicAuth("admin", "adminpassword"))
        .exchange()
        .expectStatus()
        .isEqualTo(503);
  }
}
