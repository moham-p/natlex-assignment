package com.natlex.assignment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.natlex.assignment.api.request.GeologicalClassRequest;
import com.natlex.assignment.api.request.SectionRequest;
import com.natlex.assignment.exception.JobException;
import com.natlex.assignment.util.FileUtil;

class ImportServiceTest {

  @Mock private SectionService sectionService;

  @InjectMocks private ImportService importService;

  @TempDir File tempDir;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void importFile_shouldImportSuccessfully() throws Exception {
    var request =
        SectionRequest.builder()
            .name("sectionName")
            .geologicalClasses(
                List.of(
                    GeologicalClassRequest.builder().name("class1").code("code1").build(),
                    GeologicalClassRequest.builder().name("class2").code("code2").build()))
            .build();

    File file = FileUtil.createTempExcelFile(request, tempDir);

    doNothing().when(sectionService).saveImportedSection(any(SectionRequest.class), anyString());
    CompletableFuture<String> result = importService.importFile("jobId", file.getAbsolutePath());

    assertEquals("jobId", result.get());
    verify(sectionService, times(1)).saveImportedSection(request, "jobId");
  }

  @Test
  void importFile_shouldHandleFileNotFound() {
    Exception exception =
        assertThrows(IOException.class, () -> importService.importFile("jobId", "invalid-path"));

    System.out.println(exception);
    assertTrue(exception.getMessage().contains("invalid-path"));
  }

  @Test
  void importFile_shouldHandleInvalidFileFormat() throws Exception {
    File file = File.createTempFile("test", ".xls");
    try (InputStream inputStream = new ByteArrayInputStream("invalid content".getBytes())) {
      FileUtil.writeToFile(file, inputStream);
    }

    Exception exception =
        assertThrows(
            JobException.class, () -> importService.importFile("jobId", file.getAbsolutePath()));

    assertTrue(exception.getMessage().contains("Invalid header signature"));
  }

  @Test
  void importFile_shouldHandleEmptyFile() throws Exception {

    File file = FileUtil.createEmptyExcelFile(tempDir);
    CompletableFuture<String> result = importService.importFile("jobId", file.getAbsolutePath());

    assertEquals("jobId", result.get());
    verify(sectionService, times(0)).saveImportedSection(any(SectionRequest.class), eq("jobId"));
  }

  @Test
  void importFile_shouldHandlePartialData() throws Exception {

    var request =
        SectionRequest.builder()
            .name("sectionName")
            .geologicalClasses(
                List.of(GeologicalClassRequest.builder().name("class1").code("code1").build()))
            .build();
    File file = FileUtil.createTempExcelFile(request, tempDir);
    doNothing().when(sectionService).saveImportedSection(any(SectionRequest.class), anyString());
    CompletableFuture<String> result = importService.importFile("jobId", file.getAbsolutePath());

    assertEquals("jobId", result.get());
    assertEquals("jobId", result.get());
    verify(sectionService, times(1)).saveImportedSection(request, "jobId");
  }

  @Test
  void importFile_shouldDeleteFileAfterProcessing() throws Exception {

    var request =
        SectionRequest.builder()
            .name("sectionName")
            .geologicalClasses(
                List.of(
                    GeologicalClassRequest.builder().name("class1").code("code1").build(),
                    GeologicalClassRequest.builder().name("class2").code("code2").build()))
            .build();

    File file = FileUtil.createTempExcelFile(request, tempDir);
    doNothing().when(sectionService).saveImportedSection(any(SectionRequest.class), anyString());
    importService.importFile("jobId", file.getAbsolutePath());

    assertFalse(file.exists());
  }
}
