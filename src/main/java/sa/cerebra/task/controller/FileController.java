package sa.cerebra.task.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sa.cerebra.task.entity.User;
import sa.cerebra.task.model.FileModel;
import sa.cerebra.task.security.AuthHelper;
import sa.cerebra.task.service.FileService;
import sa.cerebra.task.validation.SafePath;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Validated
public class FileController {

    private final FileService fileService;

    @GetMapping
    public ResponseEntity<List<FileModel>> listFiles(@SafePath @RequestParam(required = false) String path) {
        User user = AuthHelper.getCurrentUser();
        List<FileModel> files = fileService.listFiles(user, path);
        return ResponseEntity.ok(files);
    }


    @PostMapping
    public ResponseEntity<List<FileModel>> upload(
            @RequestParam("files") MultipartFile[] files,
            @SafePath @RequestParam(value = "path", required = false) String path) {
        User user = AuthHelper.getCurrentUser();
        List<FileModel> uploadedFiles = fileService.uploadMultipleFiles(user, files, path);
        return ResponseEntity.status(HttpStatus.CREATED).body(uploadedFiles);
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@SafePath @RequestParam String path, @RequestParam boolean preview) {
        User user = AuthHelper.getCurrentUser();
        Resource resource = fileService.downloadFile(user, path);

        if (preview) {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

}
