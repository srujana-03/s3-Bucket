package com.rest.s3.controller;


import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.io.ByteArrayInputStream;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import com.rest.s3.model.FileData;
import com.rest.s3.repository.FileRepo;
import com.rest.s3.repository.UserRepo;
import com.rest.s3.service.FileService;

@RestController
@RequestMapping("/api/files")
public class FileController {

	private final FileService fileService;
	private final FileRepo fileRepo;
	public FileController(FileService fileService, UserRepo userRepo, FileRepo fileRepo) {
		
		this.fileService = fileService;
		this.fileRepo = fileRepo;
	}

	 // Upload 
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("userId") Long userId) {
        try {
            return ResponseEntity.ok(fileService.uploadFile(file, userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

 // List Files
    @GetMapping("list")
    public Map<String, Object> listFiles(
            @RequestParam(required = false) Long userId,
            @RequestParam int page,
            @RequestParam int size) {
        return fileService.getFiles(userId, page, size);
    }


 // Download
    @GetMapping("/download/{filename}")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable String filename, @RequestParam("userId") Long userId) {
        try {
            
            InputStream fileStream = fileService.downloadFile(filename, userId);            
            String contentType = fileService.getFileContentType(filename);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(new InputStreamResource(fileStream));
        } catch (IllegalArgumentException e) {  
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new InputStreamResource(new ByteArrayInputStream(
                    e.getMessage().getBytes())));  
        } catch (Exception e) {
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    @RestControllerAdvice
    public static class GlobalExceptionHandler {

        @ExceptionHandler(MaxUploadSizeExceededException.class)
        public ResponseEntity<String> handleFileSizeLimitExceeded(MaxUploadSizeExceededException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File size exceeds the maximum limit.");
        }
    }


}
