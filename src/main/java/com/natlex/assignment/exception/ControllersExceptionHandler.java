package com.natlex.assignment.exception;

import java.io.FileNotFoundException;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class ControllersExceptionHandler {

  @ExceptionHandler(EntityNotFoundException.class)
  ProblemDetail handle(EntityNotFoundException ex) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(NoResourceFoundException.class)
  ProblemDetail handle(NoResourceFoundException ex) {
    return ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(FileNotFoundException.class)
  ProblemDetail handle(FileNotFoundException ex) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "File not found");
  }

  @ExceptionHandler(MultipartException.class)
  ProblemDetail handle(MultipartException ex) {
    return ProblemDetail.forStatusAndDetail(
        HttpStatus.BAD_REQUEST, "Current request is not a multipart request");
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  ProblemDetail handle(HttpRequestMethodNotSupportedException ex) {
    return ProblemDetail.forStatus(HttpStatus.METHOD_NOT_ALLOWED);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handle(MethodArgumentNotValidException ex) {
    Map<String, String> errors =
        ex.getBindingResult().getFieldErrors().stream()
            .collect(
                Collectors.toMap(
                    error -> error.getField(),
                    error -> error.getDefaultMessage(),
                    (existing, replacement) -> existing));
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, "One or more fields have validation errors");
    problemDetail.setProperty("errors", errors);
    return problemDetail;
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  ProblemDetail handle(MethodArgumentTypeMismatchException ex) {
    return ProblemDetail.forStatusAndDetail(
        HttpStatus.BAD_REQUEST, "Invalid value for parameter: " + ex.getPropertyName());
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  ProblemDetail handle(MissingServletRequestParameterException ex) {
    return ProblemDetail.forStatusAndDetail(
        HttpStatus.BAD_REQUEST, "Missing parameter: " + ex.getParameterName());
  }

  @ExceptionHandler(MissingServletRequestPartException.class)
  ProblemDetail handle(MissingServletRequestPartException ex) {
    return ProblemDetail.forStatusAndDetail(
        HttpStatus.BAD_REQUEST, "Missing part: " + ex.getRequestPartName());
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  ProblemDetail handle(HttpMessageNotReadableException ex) {
    return ProblemDetail.forStatusAndDetail(
        HttpStatus.BAD_REQUEST, "Required request body is missing");
  }

  @ExceptionHandler(ExportInProgressException.class)
  ProblemDetail handle(ExportInProgressException ex) {
    log.warn("Attempt to access an in-progress export");
    return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
  }

  @ExceptionHandler(Exception.class)
  ProblemDetail handle(Exception ex) {
    ex.printStackTrace();
    log.error("Internal server error: {}", ex.getMessage());
    return ProblemDetail.forStatusAndDetail(
        HttpStatus.INTERNAL_SERVER_ERROR, "Please contact the administrator");
  }
}
