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

import sa.cerebra.task.exception.CerebraException;
import sa.cerebra.task.exception.ErrorCode;
import sa.cerebra.task.model.FileModel;
import sa.cerebra.task.service.StorageService;

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

    @SneakyThrows
    @Override
    public List<FileModel> list(String userStoragePath) {
            Path path = getFullPath(userStoragePath);

//            Files.createDirectories(path);

            if (!Files.exists(path)) {
                return new ArrayList<>();
            }

            if (!Files.isDirectory(path)) {
                 return List.of(createFileModel(path));
            }

            List<FileModel> files = new ArrayList<>();
            try (Stream<Path> paths = Files.list(path)) {
                paths.forEach(p -> files.add(createFileModel(p)));
            }
            return files;
    }

    @SneakyThrows
    public FileModel uploadFile(MultipartFile file, Path path) {
            // Create directory if it doesn't exist
            Files.createDirectories(path);

            // Generate unique filename to avoid conflicts
            String originalFilename = file.getOriginalFilename();
            Path targetPath = path.resolve(originalFilename);

            // Save file
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return createFileModel(targetPath);
    }

    @SneakyThrows
    @Override
    public List<FileModel> upload( MultipartFile[] files, String userStoragePath) {
        List<FileModel> uploadedFiles = new ArrayList<>();
        Path path = getFullPath(userStoragePath);
        Files.createDirectories(path);

        for (MultipartFile file : files) {
            uploadedFiles.add(uploadFile( file, path));
        }

        return uploadedFiles;
    }

    @Override
    public Resource getResource(String filePath) {
            Path path = getFullPath(filePath);

            if (!Files.exists(path)) {
                throw new CerebraException(ErrorCode.FILE_NOT_FOUND);
            }

            if (!Files.isRegularFile(path)) {
                throw new CerebraException(ErrorCode.PATH_NOT_FILE);
            }
            return new FileSystemResource(path);
    }

    @SneakyThrows
    public Path getFullPath(String userStoragePath) {
        return Paths.get(storageBasePath, userStoragePath).normalize();
    }

    @SneakyThrows
    private FileModel createFileModel(Path path) {

        BasicFileAttributes attributes = Files.readAttributes(
                path,
                BasicFileAttributes.class
        );

        FileTime createdTime = attributes.creationTime();
        return FileModel.builder()
                .name(path.getFileName().toString())
//                .relativePath(relativePath.isEmpty() ? "" :  relativePath.replace("\\", "/"))
                .uploadDate(createdTime.toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDateTime())
                .build();

    }


}
