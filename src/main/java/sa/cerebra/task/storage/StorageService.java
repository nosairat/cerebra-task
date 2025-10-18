package sa.cerebra.task.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import sa.cerebra.task.model.FileModel;

import java.util.List;

public interface StorageService {
    List<FileModel> list(String path);

    List<FileModel> upload(MultipartFile[] files, String path);
    
    Resource getResource(String filePath);

//    Path getActualPath(User user, String filePath);

}
