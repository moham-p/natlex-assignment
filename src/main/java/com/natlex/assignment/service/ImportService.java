package com.natlex.assignment.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.natlex.assignment.api.request.GeologicalClassRequest;
import com.natlex.assignment.api.request.SectionRequest;
import com.natlex.assignment.api.request.SectionRequest.SectionRequestBuilder;
import com.natlex.assignment.exception.JobException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImportService {

  private final SectionService sectionService;

  @Async
  public CompletableFuture<String> importFile(String jobId, String filePath) throws IOException {
    File file = new File(filePath);

    try (InputStream inputStream = new FileInputStream(file);
        HSSFWorkbook workbook = new HSSFWorkbook(inputStream)) {

      HSSFSheet sheet = workbook.getSheetAt(0);
      int numberOfRows = sheet.getPhysicalNumberOfRows();
      if (numberOfRows <= 1) {
        return CompletableFuture.completedFuture(jobId);
      }

      processSheetRows(sheet, jobId);

      return CompletableFuture.completedFuture(jobId);

    } catch (IOException e) {
      throw new JobException(jobId, e.getMessage());
    } finally {
      deleteFile(file);
    }
  }

  private void processSheetRows(HSSFSheet sheet, String jobId) {
    int headerColumnsCount = sheet.getRow(0).getPhysicalNumberOfCells();
    Stream<Row> rowStream = StreamSupport.stream(sheet.spliterator(), false);

    rowStream
        .skip(1)
        .forEach(
            row -> {
              SectionRequest section = parseRowToSectionRequest(row, headerColumnsCount);
              sectionService.saveImportedSection(section, jobId);
            });
  }

  private SectionRequest parseRowToSectionRequest(Row row, int columnsCount) {
    SectionRequestBuilder sectionBuilder = SectionRequest.builder();
    sectionBuilder.name(row.getCell(0).getStringCellValue());
    List<GeologicalClassRequest> geologicalClasses = new ArrayList<>();

    for (int i = 1; i < columnsCount; i += 2) {
      Cell nameCell = row.getCell(i);
      Cell codeCell = row.getCell(i + 1);

      if (nameCell != null && codeCell != null) {
        GeologicalClassRequest geoClass =
            GeologicalClassRequest.builder()
                .name(nameCell.getStringCellValue())
                .code(codeCell.getStringCellValue())
                .build();
        geologicalClasses.add(geoClass);
      }
    }
    sectionBuilder.geologicalClasses(geologicalClasses);
    return sectionBuilder.build();
  }

  private void deleteFile(File file) {
    if (!file.delete()) {
      log.warn("Failed to delete temporary file: {}", file.getAbsolutePath());
    }
  }
}
