package com.natlex.assignment.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import jakarta.persistence.EntityNotFoundException;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.natlex.assignment.api.response.SectionResponse;
import com.natlex.assignment.exception.ExportInProgressException;
import com.natlex.assignment.exception.JobException;
import com.natlex.assignment.model.Job;
import com.natlex.assignment.model.JobState;
import com.natlex.assignment.persistence.JobRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExportService {

  private final SectionService sectionService;
  private final JobRepository jobRepository;

  @Async
  public CompletableFuture<String> exportFile(String jobId, String filePath) throws IOException {

    List<SectionResponse> sections = sectionService.getAllSections();
    HSSFWorkbook workbook = createWorkbookWithSections(sections);

    try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
      workbook.write(fileOut);
    } catch (IOException e) {
      throw new JobException(jobId, e.getMessage());
    } finally {
      workbook.close();
    }

    return CompletableFuture.completedFuture(jobId);
  }

  public File getExportFile(String jobId) {
    Job exportJob =
        jobRepository
            .findById(jobId)
            .orElseThrow(() -> new EntityNotFoundException("Job ID not found"));
    if (exportJob.getJobState() == JobState.IN_PROGRESS)
      throw new ExportInProgressException("Export is still in progress");

    return new File(exportJob.getFilePath());
  }

  private HSSFWorkbook createWorkbookWithSections(List<SectionResponse> sections) {
    HSSFWorkbook workbook = new HSSFWorkbook();
    HSSFSheet sheet = workbook.createSheet("Sections");

    int maxGeologicalClassCount = createSectionRows(sheet, sections);
    createHeaderRow(sheet, maxGeologicalClassCount);

    return workbook;
  }

  private int createSectionRows(HSSFSheet sheet, List<SectionResponse> sections) {
    int maxGeologicalClassCount = 0;

    for (int i = 0; i < sections.size(); i++) {
      SectionResponse currentSec = sections.get(i);
      Row row = sheet.createRow(i + 1);

      row.createCell(0).setCellValue(currentSec.name());
      int geologicalClassCount = currentSec.geologicalClasses().size();

      for (int j = 0; j < geologicalClassCount; j++) {
        row.createCell(j * 2 + 1).setCellValue(currentSec.geologicalClasses().get(j).name());
        row.createCell(j * 2 + 2).setCellValue(currentSec.geologicalClasses().get(j).code());
      }

      if (geologicalClassCount > maxGeologicalClassCount) {
        maxGeologicalClassCount = geologicalClassCount;
      }
    }

    return maxGeologicalClassCount;
  }

  private void createHeaderRow(HSSFSheet sheet, int maxGeologicalClassCount) {
    Row headerRow = sheet.createRow(0);
    headerRow.createCell(0).setCellValue("Section name");

    for (int i = 0; i < maxGeologicalClassCount; i++) {
      headerRow.createCell(i * 2 + 1).setCellValue("Class name");
      headerRow.createCell(i * 2 + 2).setCellValue("Class code");
    }
  }
}
