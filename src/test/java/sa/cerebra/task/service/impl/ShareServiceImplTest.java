package sa.cerebra.task.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.io.Resource;
import sa.cerebra.task.cache.CacheStore;
import sa.cerebra.task.dto.request.CreateShareLinkRequest;
import sa.cerebra.task.dto.response.ShareLinkResponse;
import sa.cerebra.task.entity.User;
import sa.cerebra.task.exception.CerebraException;
import sa.cerebra.task.exception.ErrorCode;
import sa.cerebra.task.sms.SendSms;
import sa.cerebra.task.storage.StorageService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class ShareServiceImplTest {

    @Mock
    private SendSms sendSms;

    @Mock
    private StorageService storageService;

    @Mock
    private CacheStore cacheStore;

    @Mock
    private Resource resource;

    @InjectMocks
    private ShareServiceImpl shareService;

    @Captor
    private ArgumentCaptor<String> tokenCaptor;

    private User user;

    @BeforeEach
    void setUp() throws Exception {
        // Inject baseUrl via reflection since it's @Value injected in prod
        var field = ShareServiceImpl.class.getDeclaredField("baseUrl");
        field.setAccessible(true);
        field.set(shareService, "http://localhost:8080");

        user = new User();
        user.setId(5L);
        user.setPhone("+1234567890");
    }

    @Test
    void shareLink_ShouldCachePath_AndReturnShareUrl_WithoutSms_WhenNoRecipient() {
        // Given
        CreateShareLinkRequest request = new CreateShareLinkRequest();
        request.setPath("docs/file.txt");
        request.setExpirationDays(3);
        request.setRecipientPhone(null);

        // When
        ShareLinkResponse response = shareService.shareLink(user, request);

        // Then
        verify(cacheStore).put(eq("share-links"), anyString(), eq("5/docs/file.txt"), eq(3 * 24 * 60 * 60 * 1000L));
        assertNotNull(response);
        assertNotNull(response.getLink());
        assertTrue(response.getLink().startsWith("http://localhost:8080/api/v1/share/"));
        verifyNoInteractions(sendSms);
    }

    @Test
    void shareLink_ShouldSendSms_WhenRecipientProvided() {
        // Given
        CreateShareLinkRequest request = new CreateShareLinkRequest();
        request.setPath("docs/file.txt");
        request.setExpirationDays(7);
        request.setRecipientPhone("+1111111111");

        // When
        ShareLinkResponse response = shareService.shareLink(user, request);

        // Then
        verify(cacheStore).put(eq("share-links"), anyString(), eq("5/docs/file.txt"), eq(7 * 24 * 60 * 60 * 1000L));
        verify(sendSms).send(eq("+1111111111"), contains("http://localhost:8080/api/v1/share/"));
        assertNotNull(response.getLink());
    }

    @Test
    void shareLink_ShouldHandleDifferentPathsAndExpirations() {
        // Given
        CreateShareLinkRequest request = new CreateShareLinkRequest();
        request.setPath("reports/2025/jan.pdf");
        request.setExpirationDays(1);

        // When
        shareService.shareLink(user, request);

        // Then
        verify(cacheStore).put(eq("share-links"), anyString(), eq("5/reports/2025/jan.pdf"), eq(1 * 24 * 60 * 60 * 1000L));
    }

    @Test
    void download_ShouldFetchResource_WhenTokenValid() {
        // Given
        String token = "abc";
        when(cacheStore.get("share-links", token)).thenReturn("5/docs/file.txt");
        when(storageService.getResource("5/docs/file.txt")).thenReturn(resource);

        // When
        Resource result = shareService.download(token);

        // Then
        assertSame(resource, result);
        verify(storageService).getResource("5/docs/file.txt");
    }

    @Test
    void download_ShouldThrow_WhenTokenMissingOrExpired() {
        // Given
        String token = "expired";
        when(cacheStore.get("share-links", token)).thenReturn(null);

        // When / Then
        CerebraException ex = assertThrows(CerebraException.class, () -> shareService.download(token));
        assertEquals(ErrorCode.SHARE_LINK_EXPIRED, ex.getErrorCode());
        verifyNoInteractions(storageService);
    }
}
