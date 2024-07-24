package com.natlex.assignment.exception;

import java.io.IOException;

import lombok.Getter;

@Getter
public class JobException extends IOException {

  private String jobId;

  public JobException(String jobId, String message) {
    super(message);
    this.jobId = jobId;
  }
}
