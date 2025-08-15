package com.wannawalk.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.wannawalk.backend.service.FileStorageService;

import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
       
            // Save the file using the storage service
            String fileName = fileStorageService.storeFile(file);

            // Create the full URI for accessing the file
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/files/download/")
                    .path(fileName)
                    .toUriString();

            // Return the URL in the response
            return ResponseEntity.ok(Map.of("url", fileDownloadUri));
       
    }
    
    // Note: You would also need an endpoint to serve/download the files,
    // or configure your server to serve static files from the 'uploads' directory.
}
