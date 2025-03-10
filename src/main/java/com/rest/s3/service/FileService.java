package com.rest.s3.service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Map; 
import java.util.LinkedHashMap; 
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.rest.s3.model.FileData;
import com.rest.s3.model.UserData;
import com.rest.s3.repository.FileRepo;
import com.rest.s3.repository.UserRepo;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
public class FileService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    private final FileRepo fileRepo;
    private final UserRepo userRepo;

    public FileService(S3Client s3Client, String bucketName, FileRepo fileRepo, UserRepo userRepo) {
        super();
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.fileRepo = fileRepo;
        this.userRepo = userRepo;
    }

    // Upload file and save metadata
    public String uploadFile(MultipartFile file, Long userId) throws IOException {
        validateFile(file);
        if (userId < 1) {
            throw new IllegalArgumentException("User ID should be positive.");
        }
        UserData user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user"));

        String fileName = file.getOriginalFilename();
        String contentType = file.getContentType();

        saveFileMetadataAndUploadFileToS3(file, user, fileName, contentType);

        return "File uploaded successfully: " + fileName;
    }

    // Validate file type and filename
    private void validateFile(MultipartFile file) {
        String fileType = file.getContentType();
        if (fileType == null || (!fileType.equals("image/jpeg") && !fileType.equals("image/png") &&
                !fileType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))) {
            throw new IllegalArgumentException("Invalid file type. Only JPG, PNG, and DOCX files are allowed.");
        }
        if (file.getOriginalFilename() == null) {
            throw new IllegalArgumentException("File name is invalid");
        }
    }

    // Save file metadata in the database
    private void saveFileMetadataAndUploadFileToS3(MultipartFile file, UserData user, String fileName, String contentType) throws IOException {
        FileData fileData = new FileData();
        fileData.setFileName(fileName);
        fileData.setFileType(contentType);
        fileData.setLastUpdatedOn(LocalDateTime.now());
        fileData.setUser(user);

        fileRepo.save(fileData);

        Long attachmentId = fileData.getId();
        String newFileName = attachmentId + "_" + fileName;  // Unique file name using attachmentId
        InputStream fileInputStream = file.getInputStream();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(newFileName)
                .contentType(contentType)
                .build();

        try {
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(fileInputStream, file.getSize()));
        } catch (S3Exception e) {
            throw new IllegalArgumentException("Error occurred during file upload: " + e.awsErrorDetails().errorMessage());
        }
    }

    // Get files with pagination and optional userId filter
    public Map<String, Object> getFiles(Long userId, int page, int size) {
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 15;
        }

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Order.desc("lastUpdatedOn")));
        Page<FileData> filePage;

        if (userId != null) {
            if (userId < 1) {
                throw new IllegalArgumentException("userId should be positive");
            }
            filePage = fileRepo.findByUserId(userId, pageable);
        } else {
            filePage = fileRepo.findAll(pageable);
        }
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("totalCount", filePage.getTotalElements());
        response.put("pageSize", filePage.getSize());
        response.put("totalPages", filePage.getTotalPages());
        response.put("currentPage", filePage.getNumber() + 1);
        response.put("files", filePage.getContent());

        return response;
    }

    // Download file if authorized
    public InputStream downloadFile(String filename, Long userId, Long attachmentId) throws IOException {
        if (userId < 1) {
            throw new IllegalArgumentException("User ID should be positive.");
        }

        
        Optional<FileData> fileDataOptional = fileRepo.findById(attachmentId);
        FileData fileData = fileDataOptional.orElseThrow(() ->
                new IllegalArgumentException("No record found with the given attachmentId: " + attachmentId));

       
        Optional<UserData> userOptional = userRepo.findById(userId);
        UserData user = userOptional.orElseThrow(() ->
                new IllegalArgumentException("No record found with the given userId: " + userId));

        
        if (!fileData.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("User with ID " + userId + " does not have access to the file: " + filename);
        }

        
        String newFileName = attachmentId + "_" + filename;

        
        return getFileFromS3(newFileName);
    }

    // Retrieve the file from S3
    private InputStream getFileFromS3(String newFileName) throws IOException {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(newFileName)
                    .build();

            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
            return s3Object;
        } catch (S3Exception e) {
            throw new IOException("Error occurred while fetching file from S3: " + e.awsErrorDetails().errorMessage());
        }
    }

    // Get content type based on filename extension
    public String getFileContentType(String filename) {
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (filename.endsWith(".png")) {
            return "image/png";
        } else if (filename.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }
        return "application/octet-stream";  // Default content type
    }
}
