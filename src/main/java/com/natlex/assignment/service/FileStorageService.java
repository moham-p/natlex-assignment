package com.natlex.assignment.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

  public File saveFile(MultipartFile multipartFile) throws IOException {

    File tempFile = File.createTempFile(multipartFile.getOriginalFilename() + "_upload_", ".tmp");
    InputStream inputStream = multipartFile.getInputStream();
    try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
      int read;
      byte[] bytes = new byte[1024];
      while ((read = inputStream.read(bytes)) != -1) {
        outputStream.write(bytes, 0, read);
      }
    }
    return tempFile;
  }
}
