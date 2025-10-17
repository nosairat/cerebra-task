package sa.cerebra.task.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sa.cerebra.task.entity.User;
import sa.cerebra.task.exception.FileNotFoundException;
import sa.cerebra.task.exception.FileStorageException;
import sa.cerebra.task.exception.InvalidFileException;
import sa.cerebra.task.model.FileModel;
import sa.cerebra.task.service.StorageService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalStorage implements StorageService {

    @Value("${app.storage.path:tmp/cerebra-storage}")
    private String storageBasePath;

    @Override
    public List<FileModel> list(User user, String path) {
        try {
            Path userStoragePath = getUserStoragePath(user);
            Path targetPath = path == null || path.isEmpty() ?
                    userStoragePath : userStoragePath.resolve(path);

            if (!Files.exists(targetPath)) {
                return new ArrayList<>();
            }

            if (!Files.isDirectory(targetPath)) {
                return List.of(createFileModel(targetPath, userStoragePath));
            }

            List<FileModel> files = new ArrayList<>();
            try (Stream<Path> paths = Files.list(targetPath)) {
                paths.forEach(p -> files.add(createFileModel(p, userStoragePath)));
            }

            return files;
        } catch (IOException e) {
            log.error("Error listing files for user {} at path {}", user.getUsername(), path, e);
            throw new FileStorageException("Failed to list files", e);
        }
    }

    @Override
    public FileModel uploadFile(User user, MultipartFile file, String path) {
        try {

            Path userStoragePath = getUserStoragePath(user);
            Path targetDirectory = path == null || path.isEmpty() ?
                    userStoragePath : userStoragePath.resolve(path);

            // Create directory if it doesn't exist
            Files.createDirectories(targetDirectory);

            // Generate unique filename to avoid conflicts
            String originalFilename = file.getOriginalFilename();
            Path targetPath = targetDirectory.resolve(originalFilename);

            // Save file
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return createFileModel(targetPath, userStoragePath);
        } catch (IOException e) {
            log.error("Error uploading file {} for user {}", file.getOriginalFilename(), user.getUsername(), e);
            throw new FileStorageException("Failed to upload file", e);
        }
    }

    @Override
    public List<FileModel> uploadMultipleFiles(User user, MultipartFile[] files, String path) {
        List<FileModel> uploadedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            uploadedFiles.add(uploadFile(user, file, path));

        }

        return uploadedFiles;
    }

    @Override
    public Resource downloadFile(User user, String filePath) {
        try {
            Path userStoragePath = getUserStoragePath(user);
            Path targetPath = userStoragePath.resolve(filePath);

            if (!Files.exists(targetPath)) {
                throw new FileNotFoundException("File not found");
            }

            if (!Files.isRegularFile(targetPath)) {
                throw new InvalidFileException("Path is not a file");
            }

            // Verify the file is within user's storage directory
            if (!targetPath.normalize().startsWith(userStoragePath.normalize())) {
                throw new InvalidFileException("Access denied");
            }

            return new FileSystemResource(targetPath);
        } catch (Exception e) {
            log.error("Error downloading file {} for user {}", filePath, user.getUsername(), e);
            throw new FileStorageException("Failed to download file", e);
        }
    }

    private Path getUserStoragePath(User user) throws IOException {
        Path userPath = Paths.get(storageBasePath, user.getId().toString());
        Files.createDirectories(userPath);
        return userPath;
    }

    @SneakyThrows
    private FileModel createFileModel(Path path, Path userStoragePath) {

        String relativePath = userStoragePath.relativize(path).toString();

        BasicFileAttributes attributes = Files.readAttributes(
                path,
                BasicFileAttributes.class
        );

        FileTime createdTime = attributes.creationTime();
        return FileModel.builder()
                .name(path.getFileName().toString())
                .relativePath(relativePath.isEmpty() ? "" :  relativePath.replace("\\", "/"))
                .uploadDate(createdTime.toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDateTime())
                .build();

    }


}
