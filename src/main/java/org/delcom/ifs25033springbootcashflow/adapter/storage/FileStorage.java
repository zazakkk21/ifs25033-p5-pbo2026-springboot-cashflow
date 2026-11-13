package org.delcom.ifs25033springbootcashflow.adapter.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class FileStorage {
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".png", ".jpg", ".jpeg", ".gif", ".webp");
    private final Path root;

    public FileStorage(String uploadDir) {
        root = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    public Path root() {
        return root;
    }

    public String store(InputStream input, String originalFilename) throws IOException {
        String extension = extensionOf(originalFilename);
        Files.createDirectories(root);
        String fileName = UUID.randomUUID() + extension;
        Files.copy(input, root.resolve(fileName));
        return fileName;
    }

    public void delete(String relativePath) throws IOException {
        if (relativePath == null || relativePath.isBlank()) {
            return;
        }
        Path candidate = root.resolve(relativePath).normalize();
        if (!candidate.startsWith(root)) {
            throw new IllegalArgumentException("Path file di luar direktori upload");
        }
        Files.deleteIfExists(candidate);
    }

    private String extensionOf(String originalFilename) {
        if (originalFilename == null) {
            throw new IllegalArgumentException("Nama file tidak valid");
        }
        String filename = Path.of(originalFilename).getFileName().toString();
        int dot = filename.lastIndexOf('.');
        String extension = dot >= 0 ? filename.substring(dot).toLowerCase(Locale.ROOT) : "";
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Gunakan file gambar PNG, JPG, GIF, atau WEBP");
        }
        return extension;
    }
}
