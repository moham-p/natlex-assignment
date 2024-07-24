package com.natlex.assignment.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.util.concurrent.CompletableFuture;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import com.natlex.assignment.api.controller.FileImportController;
import com.natlex.assignment.api.response.JobStateResponse;
import com.natlex.assignment.config.SecurityConfig;
import com.natlex.assignment.exception.JobException;
import com.natlex.assignment.model.Job;
import com.natlex.assignment.model.JobState;
import com.natlex.assignment.model.JobType;
import com.natlex.assignment.service.FileServiceCallback;
import com.natlex.assignment.service.FileStorageService;
import com.natlex.assignment.service.ImportService;
import com.natlex.assignment.service.JobService;

@WebMvcTest(FileImportController.class)
@Import(SecurityConfig.class)
public class FileImportControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private JobService jobService;

  @MockBean private ImportService importService;

  @MockBean private FileStorageService fileStorageService;

  @MockBean private FileServiceCallback fileImportServiceCallback;

  @Mock CompletableFuture<String> completableFuture;

  private final String url = "/api/v1/import";

  @Test
  void importFile_shouldReturnJobIdResponse() throws Exception {

    var job = Job.builder().id("123").build();

    given(fileStorageService.saveFile(any(MultipartFile.class)))
        .willReturn(File.createTempFile("uploaded", ".tmp"));
    given(jobService.saveNewFileJob(any(JobType.class))).willReturn(job);
    given(importService.importFile(anyString(), anyString()))
        .willReturn(CompletableFuture.completedFuture("123"));

    MockMultipartFile mockFile =
        new MockMultipartFile("file", "test.xls", "application/vnd.ms-excel", new byte[] {1, 2, 3});

    mockMvc
        .perform(
            multipart(url)
                .file(mockFile)
                .with(httpBasic("admin", "adminpassword"))
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(job.getId()));

    verify(importService, times(1)).importFile(anyString(), anyString());
    verify(fileImportServiceCallback, times(1)).accept("123", null);
  }

  @Test
  void importFileWithoutAuth_shouldReturn401() throws Exception {
    mockMvc.perform(post(url)).andExpect(status().is(401));
  }

  @Test
  void importFileWithInsufficientPermission_shouldReturn403() throws Exception {
    mockMvc.perform(post(url).with(httpBasic("user", "password"))).andExpect(status().is(403));
  }

  @Test
  void getImportJobState_shouldReturnJobStateResponse() throws Exception {
    var response = JobStateResponse.builder().state(JobState.DONE).build();
    given(jobService.getJobState(anyString(), any(JobType.class))).willReturn(response);

    mockMvc
        .perform(get(url + "/123").with(httpBasic("admin", "adminpassword")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.state").value(response.state().name()));
  }

  @Test
  void getImportJobStateWithNonExistingJobId_shouldReturn404() throws Exception {
    var errorMessage = "Job not found";
    given(jobService.getJobState(anyString(), any(JobType.class)))
        .willThrow(new EntityNotFoundException(errorMessage));

    mockMvc
        .perform(get(url + "/123").with(httpBasic("admin", "adminpassword")))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value(errorMessage));
  }

  @Test
  void fileImportServiceCallback_shouldGetJobExceptions() throws Exception {

    var job = Job.builder().id("123").build();
    var jobException = new JobException(job.getId(), "error occured");
    completableFuture = CompletableFuture.failedFuture(jobException);
    File tempFile = File.createTempFile("uploaded", ".tmp");

    given(importService.importFile(anyString(), anyString())).willReturn(completableFuture);
    given(fileStorageService.saveFile(any(MultipartFile.class))).willReturn(tempFile);
    given(jobService.saveNewFileJob(any(JobType.class))).willReturn(job);

    MockMultipartFile mockFile =
        new MockMultipartFile("file", "test.xls", "application/vnd.ms-excel", new byte[] {1, 2, 3});

    mockMvc
        .perform(
            multipart(url)
                .file(mockFile)
                .with(httpBasic("admin", "adminpassword"))
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(job.getId()));

    assertThat(completableFuture.isCompletedExceptionally()).isTrue();
    verify(importService, times(1)).importFile(job.getId(), tempFile.getAbsolutePath());
  }
}
