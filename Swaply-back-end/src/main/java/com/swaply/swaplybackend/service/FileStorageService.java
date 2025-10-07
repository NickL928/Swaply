package com.swaply.swaplybackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    public StoredFile store(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Unsupported file type: " + contentType);
        }

        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        String original = StringUtils.cleanPath(file.getOriginalFilename());
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0 && dot < original.length() - 1) {
            ext = original.substring(dot).toLowerCase();
        } else {
            // derive from content type if missing
            if ("image/png".equalsIgnoreCase(contentType)) ext = ".png";
            else if ("image/jpeg".equalsIgnoreCase(contentType)) ext = ".jpg";
            else if ("image/gif".equalsIgnoreCase(contentType)) ext = ".gif";
            else if ("image/webp".equalsIgnoreCase(contentType)) ext = ".webp";
        }

        String fileName = Instant.now().toEpochMilli() + "-" + UUID.randomUUID() + ext;
        Path target = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        StoredFile stored = new StoredFile();
        stored.setFileName(fileName);
        stored.setRelativeUrl("/uploads/" + fileName);
        stored.setContentType(contentType);
        stored.setSize(file.getSize());
        return stored;
    }

    public static class StoredFile {
        private String fileName;
        private String relativeUrl;
        private String contentType;
        private long size;

        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        public String getRelativeUrl() { return relativeUrl; }
        public void setRelativeUrl(String relativeUrl) { this.relativeUrl = relativeUrl; }
        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }
        public long getSize() { return size; }
        public void setSize(long size) { this.size = size; }
    }
}

