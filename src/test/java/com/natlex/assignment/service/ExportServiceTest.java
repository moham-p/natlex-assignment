package com.natlex.assignment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.natlex.assignment.api.request.GeologicalClassRequest;
import com.natlex.assignment.api.response.SectionResponse;

class ExportServiceTest {

  @Mock private SectionService sectionService;

  @InjectMocks private ExportService exportService;

  @TempDir File tempDir;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void exportFile_shouldExportSuccessfully() throws Exception {

    String jobId = "jobId123";

    List<SectionResponse> sections =
        List.of(
            SectionResponse.builder()
                .name("Section1")
                .geologicalClasses(
                    List.of(
                        GeologicalClassRequest.builder().name("n1").code("c1").build(),
                        GeologicalClassRequest.builder().name("n2").code("c2").build()))
                .build(),
            SectionResponse.builder()
                .name("Section2")
                .geologicalClasses(
                    List.of(
                        GeologicalClassRequest.builder().name("n3").code("c3").build(),
                        GeologicalClassRequest.builder().name("n4").code("c4").build(),
                        GeologicalClassRequest.builder().name("n2").code("c2").build()))
                .build());
    given(sectionService.getAllSections()).willReturn(sections);

    File file = new File(tempDir, "test.xls");
    CompletableFuture<String> result = exportService.exportFile(jobId, file.getAbsolutePath());

    assertEquals(jobId, result.get());
    verify(sectionService, times(1)).getAllSections();

    try (FileInputStream fis = new FileInputStream(file);
        Workbook workbook = new HSSFWorkbook(fis)) {
      Sheet sheet = workbook.getSheet("Sections");
      assertNotNull(sheet);
      assertEquals("Section name", sheet.getRow(0).getCell(0).getStringCellValue());
      assertEquals("Class name", sheet.getRow(0).getCell(1).getStringCellValue());
      assertEquals("Class code", sheet.getRow(0).getCell(2).getStringCellValue());
      assertEquals("Section1", sheet.getRow(1).getCell(0).getStringCellValue());
      assertEquals("n1", sheet.getRow(1).getCell(1).getStringCellValue());
      assertEquals("c1", sheet.getRow(1).getCell(2).getStringCellValue());
      assertEquals("n2", sheet.getRow(1).getCell(3).getStringCellValue());
      assertEquals("c2", sheet.getRow(1).getCell(4).getStringCellValue());
      assertEquals("Section2", sheet.getRow(2).getCell(0).getStringCellValue());
      assertEquals("n3", sheet.getRow(2).getCell(1).getStringCellValue());
      assertEquals("c3", sheet.getRow(2).getCell(2).getStringCellValue());
      assertEquals("n4", sheet.getRow(2).getCell(3).getStringCellValue());
      assertEquals("c4", sheet.getRow(2).getCell(4).getStringCellValue());
      assertEquals("n2", sheet.getRow(2).getCell(5).getStringCellValue());
      assertEquals("c2", sheet.getRow(2).getCell(6).getStringCellValue());
    }
  }

  @Test
  void exportFile_shouldHandleEmptySectionsList() throws Exception {

    String jobId = "jobId123";

    List<SectionResponse> sections = List.of();
    given(sectionService.getAllSections()).willReturn(sections);

    File file = new File(tempDir, "test.xls");
    CompletableFuture<String> result = exportService.exportFile(jobId, file.getAbsolutePath());

    assertEquals(jobId, result.get());
    verify(sectionService, times(1)).getAllSections();

    try (FileInputStream fis = new FileInputStream(file);
        Workbook workbook = new HSSFWorkbook(fis)) {
      Sheet sheet = workbook.getSheet("Sections");
      assertNotNull(sheet);
      assertEquals("Section name", sheet.getRow(0).getCell(0).getStringCellValue());
    }
  }

  @Test
  void exportFile_shouldHandleSingleSectionWithNoGeologicalClasses() throws Exception {

    String jobId = "jobId123";

    List<SectionResponse> sections =
        List.of(SectionResponse.builder().name("Section1").geologicalClasses(List.of()).build());

    given(sectionService.getAllSections()).willReturn(sections);

    File file = new File(tempDir, "test.xls");
    CompletableFuture<String> result = exportService.exportFile(jobId, file.getAbsolutePath());

    assertEquals(jobId, result.get());
    verify(sectionService, times(1)).getAllSections();

    try (FileInputStream fis = new FileInputStream(file);
        Workbook workbook = new HSSFWorkbook(fis)) {
      Sheet sheet = workbook.getSheet("Sections");
      assertNotNull(sheet);
      assertEquals("Section name", sheet.getRow(0).getCell(0).getStringCellValue());
      assertEquals("Section1", sheet.getRow(1).getCell(0).getStringCellValue());
    }
  }
}
