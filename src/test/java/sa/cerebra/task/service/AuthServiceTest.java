package sa.cerebra.task.service;

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
import org.springframework.security.core.userdetails.UserDetailsService;
import sa.cerebra.task.cache.CacheStore;
import sa.cerebra.task.dto.response.TokenResponse;
import sa.cerebra.task.entity.User;
import sa.cerebra.task.exception.CerebraException;
import sa.cerebra.task.exception.ErrorCode;
import sa.cerebra.task.helper.JwtHelper;
import sa.cerebra.task.helper.OtpHelper;
import sa.cerebra.task.sms.SendSms;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock
    private CacheStore cacheStore;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private JwtHelper jwtHelper;

    @Mock
    private SendSms sendSms;

    private AuthService authService;

    @Captor
    private ArgumentCaptor<String> otpCaptor;

    @Captor
    private ArgumentCaptor<String> smsMessageCaptor;

    @BeforeEach
    void setUp() {
        authService = new AuthService(cacheStore, userDetailsService, jwtHelper, sendSms);
    }

    @Test
    void login_ShouldGenerateOtp_SaveToCache_AndSendSms() {
        // Given
        String phone = "+1234567890";

        // When
        authService.login(phone);

        // Then
        verify(cacheStore).put(eq("login-otp"), eq(phone), otpCaptor.capture(), eq(Long.parseLong(OtpHelper.OTP_EXPIRY_MINUTES+"") ));
        String generatedOtp = otpCaptor.getValue();
        assertNotNull(generatedOtp);
        assertEquals(6, generatedOtp.length());
        assertTrue(generatedOtp.matches("\\d{6}"));

        verify(sendSms).send(eq(phone), smsMessageCaptor.capture());
        String smsMessage = smsMessageCaptor.getValue();
        assertNotNull(smsMessage);
        assertTrue(smsMessage.contains(generatedOtp));
    }

    @Test
    void validate_ShouldThrowInvalidOtp_WhenOtpDoesNotMatch() {
        // Given
        String phone = "+1234567890";
        String providedOtp = "111111";
        String storedOtp = "222222";
        when(cacheStore.get("login-otp", phone)).thenReturn(storedOtp);

        // When / Then
        CerebraException ex = assertThrows(CerebraException.class, () -> authService.validate(phone, providedOtp));
        assertEquals(ErrorCode.INVALID_OTP, ex.getErrorCode());
        verify(cacheStore, never()).remove(anyString(), anyString());
        verifyNoInteractions(jwtHelper);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
    }

    @Test
    void validate_ShouldThrowInvalidOtp_WhenStoredOtpIsNull() {
        // Given
        String phone = "+1234567890";
        String providedOtp = "111111";
        when(cacheStore.get("login-otp", phone)).thenReturn(null);

        // When / Then
        CerebraException ex = assertThrows(CerebraException.class, () -> authService.validate(phone, providedOtp));
        assertEquals(ErrorCode.INVALID_OTP, ex.getErrorCode());
        verify(cacheStore, never()).remove(anyString(), anyString());
        verifyNoInteractions(jwtHelper);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
    }

    @Test
    void validate_ShouldRemoveOtp_LoadUser_GenerateToken_AndReturnResponse_WhenOtpMatches() {
        // Given
        String phone = "+1234567890";
        String otp = "123456";
        when(cacheStore.get("login-otp", phone)).thenReturn(otp);

        User user = new User();
        user.setId(42L);
        user.setPhone(phone);
        when(userDetailsService.loadUserByUsername(phone)).thenReturn(user);
        when(jwtHelper.generateToken(42L)).thenReturn("access.jwt.token");

        // When
        TokenResponse response = authService.validate(phone, otp);

        // Then
        verify(cacheStore).remove("login-otp", phone);
        verify(userDetailsService).loadUserByUsername(phone);
        verify(jwtHelper).generateToken(42L);

        assertNotNull(response);
        assertEquals("access.jwt.token", response.getAccessToken());
    }

    @Test
    void validate_ShouldHandleDifferentUserIds() {
        // Given
        String phone = "+1000000000";
        String otp = "654321";
        when(cacheStore.get("login-otp", phone)).thenReturn(otp);

        User user = new User();
        user.setId(7L);
        user.setPhone(phone);
        when(userDetailsService.loadUserByUsername(phone)).thenReturn(user);
        when(jwtHelper.generateToken(7L)).thenReturn("tkn.7");

        // When
        TokenResponse response = authService.validate(phone, otp);

        // Then
        assertEquals("tkn.7", response.getAccessToken());
    }
}
