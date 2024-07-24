package com.natlex.assignment.service;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.natlex.assignment.api.response.JobStateResponse;
import com.natlex.assignment.mapper.JobMapper;
import com.natlex.assignment.model.Job;
import com.natlex.assignment.model.JobState;
import com.natlex.assignment.model.JobType;
import com.natlex.assignment.persistence.JobRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobService {

  private final JobRepository jobRepository;

  @Transactional
  public Job saveNewFileJob(JobType type) throws IOException {

    Job newJob =
        Job.builder()
            .id(UUID.randomUUID().toString())
            .jobType(type)
            .jobState(JobState.IN_PROGRESS)
            .build();

    if (type.equals(JobType.EXPORT))
      newJob.setFilePath(File.createTempFile("export_", ".xls").getAbsolutePath());

    return jobRepository.save(newJob);
  }

  @Transactional
  public void updateJobState(String id, JobState state) {
    Job importJob =
        jobRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Job ID not found"));
    importJob.setJobState(state);
    jobRepository.save(importJob);
  }

  public JobStateResponse getJobState(String id, JobType type) {
    Job job =
        jobRepository
            .findByIdAndJobType(id, type)
            .orElseThrow(() -> new EntityNotFoundException("Job ID not found"));
    return JobMapper.toJobState(job);
  }
}
