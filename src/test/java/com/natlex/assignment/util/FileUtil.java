package com.natlex.assignment.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.natlex.assignment.api.request.GeologicalClassRequest;
import com.natlex.assignment.api.request.SectionRequest;

public class FileUtil {

  public static File createTempExcelFile(SectionRequest sectionRequest, File tempDir)
      throws IOException {
    File file = new File(tempDir, "test.xls");

    try (Workbook workbook = new HSSFWorkbook()) {
      Sheet sheet = workbook.createSheet("Sheet1");

      Row header = sheet.createRow(0);
      header.createCell(0).setCellValue("Section name");

      List<GeologicalClassRequest> geologicalClasses = sectionRequest.geologicalClasses();
      for (int i = 0; i < geologicalClasses.size(); i++) {
        int baseIndex = i * 2 + 1;
        header.createCell(baseIndex).setCellValue("Class " + (i + 1) + " name");
        header.createCell(baseIndex + 1).setCellValue("Class " + (i + 1) + " code");
      }

      Row row = sheet.createRow(1);
      row.createCell(0).setCellValue(sectionRequest.name());
      for (int i = 0; i < geologicalClasses.size(); i++) {
        GeologicalClassRequest geoClass = geologicalClasses.get(i);
        int baseIndex = i * 2 + 1;
        row.createCell(baseIndex).setCellValue(geoClass.name());
        row.createCell(baseIndex + 1).setCellValue(geoClass.code());
      }

      try (FileOutputStream outputStream = new FileOutputStream(file)) {
        workbook.write(outputStream);
      }
    }

    return file;
  }

  public static File createEmptyExcelFile(File tempDir) throws IOException {
    File file = new File(tempDir, "empty-test.xls");
    try (Workbook workbook = new HSSFWorkbook()) {
      workbook.createSheet("Sheet1");
      try (FileOutputStream outputStream = new FileOutputStream(file)) {
        workbook.write(outputStream);
      }
    }
    return file;
  }

  public static void writeToFile(File file, InputStream inputStream) throws IOException {
    try (FileOutputStream fos = new FileOutputStream(file)) {
      byte[] buffer = new byte[1024];
      int length;
      while ((length = inputStream.read(buffer)) > 0) {
        fos.write(buffer, 0, length);
      }
    }
  }
}
