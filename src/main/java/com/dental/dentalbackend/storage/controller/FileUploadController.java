package com.dental.dentalbackend.storage.controller;

import com.dental.dentalbackend.common.dto.ApiResponse;
import com.dental.dentalbackend.storage.dto.FileUploadResponse;
import com.dental.dentalbackend.storage.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Tag(name = "File Storage", description = "Upload files and images")
public class FileUploadController {

    private final FileStorageService fileStorageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Upload an image or document file")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "general") String folder) {

        String fileUrl = fileStorageService.storeFile(file, folder);

        FileUploadResponse response = FileUploadResponse.builder()
                .fileName(file.getOriginalFilename())
                .fileUrl(fileUrl)
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .build();

        return ResponseEntity.ok(ApiResponse.success("File uploaded successfully", response));
    }
}
