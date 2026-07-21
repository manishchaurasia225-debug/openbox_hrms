package com.ogm.hrms.storage;

import com.ogm.hrms.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Local-filesystem {@link StorageService}. Content is written under a configured base directory with
 * a generated key ({@code folder/uuid.ext}); the key is never derived from user input, and every
 * resolved path is confined to the base directory to prevent path traversal.
 */
@Service
public class LocalStorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalStorageService.class);

    private final Path root;

    public LocalStorageService(StorageProperties properties) {
        this.root = Paths.get(properties.basePath()).toAbsolutePath().normalize();
    }

    @Override
    public String store(String folder, String originalFilename, byte[] content) {
        String key = (isBlank(folder) ? "misc" : sanitize(folder)) + "/" + UUID.randomUUID() + extension(originalFilename);
        Path target = resolve(key);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, content);
            return key;
        } catch (IOException e) {
            throw new StorageException("Failed to store document content", e);
        }
    }

    @Override
    public Resource load(String storageKey) {
        Resource resource = new FileSystemResource(resolve(storageKey));
        if (!resource.exists() || !resource.isReadable()) {
            throw new ResourceNotFoundException("Document content", "key", storageKey);
        }
        return resource;
    }

    @Override
    public void delete(String storageKey) {
        try {
            Files.deleteIfExists(resolve(storageKey));
        } catch (IOException e) {
            log.warn("Failed to delete stored content for key {}: {}", storageKey, e.getMessage());
        }
    }

    private Path resolve(String key) {
        Path target = root.resolve(key).normalize();
        if (!target.startsWith(root)) {
            throw new StorageException("Resolved storage path escapes the base directory: " + key, null);
        }
        return target;
    }

    private String sanitize(String folder) {
        return folder.replaceAll("[^a-zA-Z0-9/_-]", "_");
    }

    private String extension(String filename) {
        if (filename == null) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : "";
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
