package sa.cerebra.task.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sa.cerebra.task.entity.User;
import sa.cerebra.task.exception.CerebraException;
import sa.cerebra.task.model.FileModel;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {
    private final StorageService storageService;

    public List<FileModel> listFiles(User user, String path) {
        log.info("Listing files for user {} at path {}", user.getUsername(), path);
        return storageService.list(user, path);
    }

    public List<FileModel> uploadMultipleFiles(User user, MultipartFile[] files, String path) {
        log.info("Uploading {} files for user {} to path {}", files.length, user.getUsername(), path);
        for (MultipartFile file : files) {
            validateFile(file);
        }
        return storageService.uploadMultipleFiles(user, files, path);
    }

    public Resource downloadFile(User user, String filePath) {
        log.info("Downloading file {} for user {}", filePath, user.getUsername());
        return storageService.downloadFile(user, filePath);
    }


    private void validateFile(MultipartFile file) {
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new CerebraException("File must have a name");
        }
        
        // Check for path traversal attempts
        if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
            throw new CerebraException("Invalid file name");
        }
        
        // Check file size (e.g., 100MB limit)
        if (file.getSize() > 100 * 1024 * 1024) {
            throw new CerebraException("File size exceeds maximum limit of 100MB");
        }
    }

}
