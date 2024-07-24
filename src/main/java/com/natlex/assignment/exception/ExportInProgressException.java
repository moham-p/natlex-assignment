package com.natlex.assignment.exception;

public class ExportInProgressException extends RuntimeException {
  public ExportInProgressException(String message) {
    super(message);
  }
}
