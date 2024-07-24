package com.natlex.assignment.service;

import java.util.function.BiConsumer;

import org.springframework.stereotype.Component;

import com.natlex.assignment.exception.JobException;
import com.natlex.assignment.model.JobState;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileServiceCallback implements BiConsumer<String, Throwable> {

  private final JobService jobService;

  @Override
  public void accept(String jobId, Throwable ex) {
    if (ex == null) {
      jobService.updateJobState(jobId, JobState.DONE);
      log.info("Job {} completed", jobId);
    } else {
      if (ex.getCause() instanceof JobException) {
        jobId = ((JobException) ex.getCause()).getJobId();
        jobService.updateJobState(jobId, JobState.ERROR);
        log.error("Job {} failed", jobId);
      } else {
        log.error("An unexpected exception: {}", ex.getMessage());
        ex.printStackTrace();
      }
    }
  }
}
