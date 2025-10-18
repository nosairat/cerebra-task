package sa.cerebra.task.controller;

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
public class ShareController {
    
    private final ShareService shareService;
    private final StorageService storageService;
    
    @PostMapping
    public ResponseEntity<?> createShareLink(@Valid @RequestBody CreateShareLinkRequest request) {
        User user = AuthHelper.getCurrentUser();
        ShareLinkResponse response = shareService.shareLink(user, request);
        return ResponseEntity.ok(response);
    }
//
//    @GetMapping("/my-links")
//    public ResponseEntity<List<ShareLinkResponse>> getUserShareLinks() {
//        User user = AuthHelper.getCurrentUser();
//        List<ShareLinkResponse> shareLinks = shareLinkService.getUserShareLinks(user);
//        return ResponseEntity.ok(shareLinks);
//    }
    
    @GetMapping("/{shareToken}")
    public ResponseEntity<?> getShareLink(@PathVariable String shareToken) {
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
