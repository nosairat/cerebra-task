package sa.cerebra.task.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sa.cerebra.task.cache.CacheStore;
import sa.cerebra.task.dto.request.CreateShareLinkRequest;
import sa.cerebra.task.dto.response.ShareLinkResponse;
import sa.cerebra.task.entity.User;
import sa.cerebra.task.exception.CerebraException;
import sa.cerebra.task.exception.ErrorCode;
import sa.cerebra.task.helper.PathHelper;
import sa.cerebra.task.sms.SendSms;
import sa.cerebra.task.service.ShareService;
import sa.cerebra.task.storage.StorageService;

import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShareServiceImpl implements ShareService {
    
    private final SendSms sendSms;
    private final StorageService storageService;
    private final CacheStore cacheStore;
    private final String cacheName = "share-links";

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Override
    @Transactional
    public ShareLinkResponse shareLink(User user, CreateShareLinkRequest request) {
        log.info("Creating share link for user {} and file {}", user.getUsername(), request.getPath());
        String userStoragePath = PathHelper.getUserStoragePath(user, request.getPath());

        String shareToken = generateSecureToken();
        

        cacheStore.put(cacheName, shareToken, userStoragePath, request.getExpirationDays()* 24*60*60*1000);

        String downloadUrl = baseUrl + "/api/v1/share/" + shareToken;

        // Send SMS notification if phone number provided
        if (request.getRecipientPhone() != null && !request.getRecipientPhone().trim().isEmpty()) {

            sendShareNotification(request.getRecipientPhone(), downloadUrl, request.getExpirationDays());
        }
        
        return new ShareLinkResponse().setLink(downloadUrl);
    }

    @Override
    public Resource download(String shareToken) {
        Object o = cacheStore.get(cacheName, shareToken);
        if(o == null) {
            throw new CerebraException(ErrorCode.SHARE_LINK_EXPIRED);
        }
        Resource resource = storageService.getResource((String) o);
        return resource;
    }


    private String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    

    private void sendShareNotification(String phoneNumber, String downloadUrl, Integer expirationDays) {
            String message = String.format(
                "You have received a shared file! Download it using this link: %s\nThis link will expire in %s days.",
                downloadUrl, expirationDays
            );
            
            sendSms.send(phoneNumber, message);
            log.info("SMS notification sent to {} for share link {}", phoneNumber, downloadUrl);

    }
}
