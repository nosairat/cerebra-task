package sa.cerebra.task.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sa.cerebra.task.entity.User;
import sa.cerebra.task.exception.CerebraException;
import sa.cerebra.task.exception.ErrorCode;
import sa.cerebra.task.helper.PathHelper;
import sa.cerebra.task.model.FileModel;

import java.io.File;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {
    private final StorageService storageService;

    public List<FileModel> listFiles(User user, String relativeUserPath) {
        log.info("Listing files for user {} at path {}", user.getUsername(), relativeUserPath);
        String storagePath = PathHelper.getUserStoragePath(user, relativeUserPath);
        List<FileModel> list = storageService.list(storagePath);
        return fillModelDetails(list, relativeUserPath);
    }

    public List<FileModel> uploadMultipleFiles(User user, MultipartFile[] files, String relativeUserPath) {
        log.info("Uploading {} files for user {} to path {}", files.length, user.getUsername(), relativeUserPath);
        for (MultipartFile file : files) {
            validateFile(file);
        }
        String storagePath = PathHelper.getUserStoragePath(user, relativeUserPath);

        List<FileModel> upload = storageService.upload(files, storagePath);
        return fillModelDetails(upload, relativeUserPath);
    }

    public Resource downloadFile(User user, String relativeUserPath) {
        log.info("Downloading file {} for user {}", relativeUserPath, user.getUsername());
        String storagePath = PathHelper.getUserStoragePath(user, relativeUserPath);

        return storageService.getResource(storagePath);
    }


    private void validateFile(MultipartFile file) {
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new CerebraException(ErrorCode.FILE_NAME_REQUIRED);
        }
        
        // Check for path traversal attempts
        if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
            throw new CerebraException(ErrorCode.INVALID_FILE_NAME);
        }
        
        // Check file size (e.g., 100MB limit)
        if (file.getSize() > 100 * 1024 * 1024) {
            throw new CerebraException(ErrorCode.FILE_SIZE_EXCEEDED);
        }
    }

    private List<FileModel> fillModelDetails(List<FileModel> list, String relativeUserPath) {
        String relativePath = "";
        if (!Strings.isBlank(relativeUserPath) && !Strings.isEmpty(relativeUserPath))
            relativePath = relativeUserPath.concat(File.separator);
        for (var q : list)
            q.setRelativePath(relativePath.concat(q.getName()));
        return list;
    }
}
