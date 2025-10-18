package sa.cerebra.task.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "File Management", description = "File upload, download, and management APIs")
@SecurityRequirement(name = "bearerAuth")
public class FileController {

    private final FileService fileService;

    @Operation(
            summary = "List files",
            description = "Get a list of files in the specified directory path"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", 
                    description = "Files listed successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = FileModel.class))
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/UnauthorizedErrorResponse"))),
            @ApiResponse(responseCode = "400", description = "Invalid path format",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ValidationErrorResponse"))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/InternalErrorResponse")))
    })
    @GetMapping
    public ResponseEntity<List<FileModel>> listFiles(
            @Parameter(description = "Directory path to list files from", example = "/documents")
            @SafePath @RequestParam(required = false) String path) {
        User user = AuthHelper.getCurrentUser();
        List<FileModel> files = fileService.listFiles(user, path);
        return ResponseEntity.ok(files);
    }

    @Operation(
            summary = "Upload files",
            description = "Upload multiple files to the specified directory path"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201", 
                    description = "Files uploaded successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = FileModel.class))
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/UnauthorizedErrorResponse"))),
            @ApiResponse(responseCode = "400", description = "Invalid file or path format",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/FileSizeExceededErrorResponse"))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/InternalErrorResponse")))
    })
    @PostMapping
    public ResponseEntity<List<FileModel>> upload(
            @Parameter(description = "Files to upload", required = true)
            @RequestParam("files") MultipartFile[] files,
            @Parameter(description = "Directory path to upload files to", example = "/documents")
            @SafePath @RequestParam(value = "path", required = false) String path) {
        User user = AuthHelper.getCurrentUser();
        List<FileModel> uploadedFiles = fileService.uploadMultipleFiles(user, files, path);
        return ResponseEntity.status(HttpStatus.CREATED).body(uploadedFiles);
    }

    @Operation(
            summary = "Download file",
            description = "Download a file from the specified path"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", 
                    description = "File downloaded successfully",
                    content = @Content(mediaType = "application/octet-stream")
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/UnauthorizedErrorResponse"))),
            @ApiResponse(responseCode = "404", description = "File not found",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/FileNotFoundErrorResponse"))),
            @ApiResponse(responseCode = "400", description = "Invalid path format",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ValidationErrorResponse"))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/InternalErrorResponse")))
    })
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "File path to download", required = true, example = "/documents/file.pdf")
            @SafePath @RequestParam String path, 
            @Parameter(description = "Preview mode - if true, file will be displayed inline instead of downloaded")
            @RequestParam(required = false) boolean preview) {
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
