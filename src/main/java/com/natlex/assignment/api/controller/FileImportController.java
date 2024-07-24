package com.natlex.assignment.api.controller;

import java.io.File;
import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.natlex.assignment.api.response.JobIdResponse;
import com.natlex.assignment.api.response.JobStateResponse;
import com.natlex.assignment.mapper.JobMapper;
import com.natlex.assignment.model.Job;
import com.natlex.assignment.model.JobType;
import com.natlex.assignment.service.FileServiceCallback;
import com.natlex.assignment.service.FileStorageService;
import com.natlex.assignment.service.ImportService;
import com.natlex.assignment.service.JobService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/import")
@RequiredArgsConstructor
public class FileImportController {

  private final JobService jobService;
  private final ImportService importService;
  private final FileStorageService fileStorageService;
  private final FileServiceCallback fileImportServiceCallback;

  @PostMapping
  public ResponseEntity<JobIdResponse> importFile(@RequestParam("file") MultipartFile file)
      throws IOException {

    File uploadedFile = fileStorageService.saveFile(file);
    String filePath = uploadedFile.getAbsolutePath();
    Job newJob = jobService.saveNewFileJob(JobType.IMPORT);

    importService.importFile(newJob.getId(), filePath).whenComplete(fileImportServiceCallback);
    return ResponseEntity.status(HttpStatus.CREATED).body(JobMapper.toJobId(newJob));
  }

  @GetMapping("/{id}")
  public ResponseEntity<JobStateResponse> getImportJobState(@PathVariable String id) {

    JobStateResponse response = jobService.getJobState(id, JobType.IMPORT);
    return ResponseEntity.ok().body(response);
  }
}
