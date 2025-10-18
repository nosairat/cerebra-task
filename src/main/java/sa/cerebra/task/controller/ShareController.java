package sa.cerebra.task.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sa.cerebra.task.dto.request.CreateShareLinkRequest;
import sa.cerebra.task.dto.response.ShareLinkResponse;
import sa.cerebra.task.entity.User;
import sa.cerebra.task.security.AuthHelper;
import sa.cerebra.task.service.ShareService;
import sa.cerebra.task.storage.StorageService;

@Slf4j
@RestController
@RequestMapping("/api/v1/share")
@RequiredArgsConstructor
@Tag(name = "File Sharing", description = "File sharing and link generation APIs")
@SecurityRequirement(name = "bearerAuth")
public class ShareController {
    
    private final ShareService shareService;

    @Operation(
            summary = "Create share link",
            description = "Create a shareable link for a file that can be accessed by the specified recipient"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", 
                    description = "Share link created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ShareLinkResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "File not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<?> createShareLink(@Valid @RequestBody CreateShareLinkRequest request) {
        User user = AuthHelper.getCurrentUser();
        ShareLinkResponse response = shareService.shareLink(user, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Access shared file",
            description = "Download a file using a share token (no authentication required)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", 
                    description = "File downloaded successfully",
                    content = @Content(mediaType = "application/octet-stream")
            ),
            @ApiResponse(responseCode = "404", description = "Share link not found or expired"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{shareToken}")
    public ResponseEntity<?> getShareLink(
            @Parameter(description = "Share token to access the file", required = true, example = "abc123def456")
            @PathVariable String shareToken) {
        // Download the file using the original user's context
        Resource resource = shareService.download(shareToken);

        HttpHeaders headers = new HttpHeaders();

            headers.setContentDispositionFormData("attachment", resource.getFilename());
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .headers(headers)
                    .body(resource);
    }

}
