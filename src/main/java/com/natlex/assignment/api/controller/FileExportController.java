package com.natlex.assignment.api.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.natlex.assignment.api.response.JobIdResponse;
import com.natlex.assignment.api.response.JobStateResponse;
import com.natlex.assignment.mapper.JobMapper;
import com.natlex.assignment.model.Job;
import com.natlex.assignment.model.JobType;
import com.natlex.assignment.service.ExportService;
import com.natlex.assignment.service.FileServiceCallback;
import com.natlex.assignment.service.JobService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/export")
@RequiredArgsConstructor
public class FileExportController {

  private final JobService jobService;
  private final ExportService exportService;
  private final FileServiceCallback fileServiceCallback;

  @GetMapping
  public ResponseEntity<JobIdResponse> exportFile() throws IOException {

    Job newJob = jobService.saveNewFileJob(JobType.EXPORT);
    exportService
        .exportFile(newJob.getId(), newJob.getFilePath())
        .whenComplete(fileServiceCallback);
    return ResponseEntity.ok().body(JobMapper.toJobId(newJob));
  }

  @GetMapping("/{id}")
  public ResponseEntity<JobStateResponse> getExportJobStatus(@PathVariable String id) {
    JobStateResponse response = jobService.getJobState(id, JobType.EXPORT);
    return ResponseEntity.ok().body(response);
  }

  @GetMapping("/{id}/file")
  public ResponseEntity<InputStreamResource> getExportedFile(@PathVariable String id)
      throws IOException {

    File exportedFile = exportService.getExportedFile(id);

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + id + ".xls");

    return ResponseEntity.ok()
        .headers(headers)
        .contentLength(exportedFile.length())
        .contentType(MediaType.parseMediaType(Files.probeContentType(exportedFile.toPath())))
        .body(new InputStreamResource(new FileInputStream(exportedFile)));
  }
}
