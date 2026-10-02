package com.dental.dentalbackend.storage.service;

import com.dental.dentalbackend.common.exception.BusinessRuleException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            ".jpg", ".jpeg", ".png", ".webp", ".gif", ".svg", ".pdf", ".doc", ".docx"
    );

    private Path rootLocation;

    @PostConstruct
    public void init() {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootLocation);
        } catch (IOException e) {
            log.error("Could not initialize storage directory", e);
            throw new RuntimeException("Could not initialize storage directory", e);
        }
    }

    public String storeFile(MultipartFile file, String subfolder) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String originalFilename = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");

        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalFilename.substring(dotIndex).toLowerCase();
        }

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessRuleException("File type '" + extension + "' is not supported. Allowed types: "
                    + String.join(", ", ALLOWED_EXTENSIONS));
        }

        String uniqueFilename = UUID.randomUUID() + extension;

        try {
            Path targetFolder = this.rootLocation.resolve(subfolder != null ? subfolder : "misc").normalize();
            Files.createDirectories(targetFolder);

            Path targetLocation = targetFolder.resolve(uniqueFilename);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            return "/uploads/" + (subfolder != null ? subfolder : "misc") + "/" + uniqueFilename;
        } catch (IOException e) {
            log.error("Failed to store file {}", originalFilename, e);
            throw new RuntimeException("Failed to store file: " + originalFilename, e);
        }
    }
}
