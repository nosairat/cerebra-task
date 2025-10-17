package sa.cerebra.task.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import sa.cerebra.task.entity.User;
import sa.cerebra.task.model.FileModel;

import java.util.List;

public interface StorageService {
    List<FileModel> list(User user, String path);
    
    FileModel uploadFile(User user, MultipartFile file, String path);
    
    List<FileModel> uploadMultipleFiles(User user, MultipartFile[] files, String path);
    
    Resource downloadFile(User user, String filePath);

}
