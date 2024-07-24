package com.natlex.assignment.mapper;

import java.util.Optional;

import com.natlex.assignment.api.response.JobIdResponse;
import com.natlex.assignment.api.response.JobStateResponse;
import com.natlex.assignment.model.Job;

public class JobMapper {

  public static JobStateResponse toJobState(Job job) {
    return Optional.ofNullable(job)
        .map(j -> JobStateResponse.builder().state(j.getJobState()).build())
        .orElse(null);
  }

  public static JobIdResponse toJobId(Job job) {
    return Optional.ofNullable(job)
        .map(j -> JobIdResponse.builder().id(j.getId()).build())
        .orElse(null);
  }
}
