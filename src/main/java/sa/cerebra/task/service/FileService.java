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

    public List<FileModel> listFiles(User user, String path) {
        log.info("Listing files for user {} at path {}", user.getUsername(), path);
        String userStoragePath = PathHelper.getUserStoragePath(user, path);
        List<FileModel> list = storageService.list(userStoragePath);
        return fillModelDetails(list, path);
    }

    public List<FileModel> uploadMultipleFiles(User user, MultipartFile[] files, String path) {
        log.info("Uploading {} files for user {} to path {}", files.length, user.getUsername(), path);
        for (MultipartFile file : files) {
            validateFile(file);
        }
        String userStoragePath = PathHelper.getUserStoragePath(user, path);

        List<FileModel> upload = storageService.upload(files, userStoragePath);
        return fillModelDetails(upload, path);
    }

    public Resource downloadFile(User user, String path) {
        log.info("Downloading file {} for user {}", path, user.getUsername());
        String userStoragePath = PathHelper.getUserStoragePath(user, path);

        Resource resource = storageService.getResource(userStoragePath);
        if (!resource.exists()) {
            throw new CerebraException(ErrorCode.FILE_NOT_FOUND);
        }
        return resource;
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

    private List<FileModel> fillModelDetails(List<FileModel> list, String path) {
        String relativePath = "";
        if (!Strings.isBlank(path) && !Strings.isEmpty(path))
            relativePath = path.concat(File.separator);
        for (var q : list)
            q.setPath(relativePath.concat(q.getName()));
        return list;
    }
}
