package com.natlex.assignment.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import com.natlex.assignment.api.controller.FileExportController;
import com.natlex.assignment.api.response.JobStateResponse;
import com.natlex.assignment.config.SecurityConfig;
import com.natlex.assignment.exception.ExportInProgressException;
import com.natlex.assignment.exception.JobException;
import com.natlex.assignment.model.Job;
import com.natlex.assignment.model.JobState;
import com.natlex.assignment.model.JobType;
import com.natlex.assignment.service.ExportService;
import com.natlex.assignment.service.FileServiceCallback;
import com.natlex.assignment.service.JobService;

@WebMvcTest(FileExportController.class)
@Import(SecurityConfig.class)
public class FileExportControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private JobService jobService;

  @MockBean private ExportService exportService;

  @MockBean private FileServiceCallback fileServiceCallback;

  @Mock CompletableFuture<String> completableFuture;

  private final String url = "/api/v1/export";

  @Test
  void exportFile_shouldReturnJobIdResponse() throws Exception {

    var job = Job.builder().id("123").filePath("somepath").build();

    given(jobService.saveNewFileJob(any(JobType.class))).willReturn(job);
    given(exportService.exportFile(anyString(), anyString()))
        .willReturn(CompletableFuture.completedFuture("123"));

    mockMvc
        .perform(get(url).with(httpBasic("admin", "adminpassword")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists());
  }

  @Test
  void exportFileWithoutAuth_shouldReturn401() throws Exception {
    mockMvc.perform(get(url)).andExpect(status().is(401));
  }

  @Test
  void exportFileWithInsufficientPermission_shouldReturn403() throws Exception {
    mockMvc.perform(get(url).with(httpBasic("user", "password"))).andExpect(status().is(403));
  }

  @Test
  void getExportJobStatus_shouldReturnJobStateResponse() throws Exception {
    var response = JobStateResponse.builder().state(JobState.DONE).build();
    given(jobService.getJobState(anyString(), any(JobType.class))).willReturn(response);

    mockMvc
        .perform(get(url + "/123").with(httpBasic("admin", "adminpassword")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.state").value(response.state().name()));
  }

  @Test
  void getExportJobStatusWithNonExistingJobId_shouldReturn404() throws Exception {
    var errorMessage = "Job not found";
    given(jobService.getJobState(anyString(), any(JobType.class)))
        .willThrow(new EntityNotFoundException(errorMessage));

    mockMvc
        .perform(get(url + "/123").with(httpBasic("admin", "adminpassword")))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value(errorMessage));
  }

  @Test
  void getExportedFile_shouldReturnInputStreamResource() throws Exception {
    var fileContentBytes = new byte[] {1, 2, 3};
    var exportedFile = File.createTempFile("sections", ".xls");
    Files.write(exportedFile.toPath(), fileContentBytes);

    given(exportService.getExportFile(anyString())).willReturn(exportedFile);

    mockMvc
        .perform(get(url + "/123/file").with(httpBasic("admin", "adminpassword")))
        .andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=123.xls"))
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/vnd.ms-excel"))
        .andExpect(header().longValue(HttpHeaders.CONTENT_LENGTH, fileContentBytes.length))
        .andExpect(content().bytes(fileContentBytes));
  }

  @Test
  void getExportedFileWithNonExistingJobId_shouldReturn404() throws Exception {
    var errorMessage = "Job not found";
    given(exportService.getExportFile(anyString()))
        .willThrow(new EntityNotFoundException(errorMessage));

    mockMvc
        .perform(get(url + "/123/file").with(httpBasic("admin", "adminpassword")))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value(errorMessage));
  }

  @Test
  void getExportedFileWhileInProgress_shouldReturn503() throws Exception {
    var errorMessage = "Export is still in progress";
    given(exportService.getExportFile(anyString()))
        .willThrow(new ExportInProgressException(errorMessage));

    mockMvc
        .perform(get(url + "/123/file").with(httpBasic("admin", "adminpassword")))
        .andExpect(status().is(503))
        .andExpect(jsonPath("$.detail").value(errorMessage));
  }

  @Test
  void fileExportServiceCallback_shouldGetJobExceptions() throws Exception {

    var job = Job.builder().id("123").filePath("test/file/path").build();
    var jobException = new JobException(job.getId(), "error occured");
    completableFuture = CompletableFuture.failedFuture(jobException);

    given(exportService.exportFile(anyString(), anyString())).willReturn(completableFuture);
    given(jobService.saveNewFileJob(any(JobType.class))).willReturn(job);

    mockMvc
        .perform(get(url).with(httpBasic("admin", "adminpassword")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(job.getId()));

    assertThat(completableFuture.isCompletedExceptionally()).isTrue();
    verify(exportService, times(1)).exportFile(job.getId(), job.getFilePath());
  }
}
