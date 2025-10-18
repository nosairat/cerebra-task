package sa.cerebra.task.security;

import org.junit.jupiter.api.Test;
import sa.cerebra.task.cache.CacheStore;
import sa.cerebra.task.dto.response.TokenResponse;
import sa.cerebra.task.entity.User;
import sa.cerebra.task.exception.CerebraException;
import sa.cerebra.task.sms.SendSms;

import org.springframework.security.core.userdetails.UserDetailsService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class AuthServiceTests {

    @Test
    void login_cachesOtp_and_sendsSms() {
        CacheStore redis = mock(CacheStore.class);
        UserDetailsService uds = mock(UserDetailsService.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);
        SendSms sms = mock(SendSms.class);

        AuthService service = new AuthService(redis, uds, jwtUtil, sms);
        service.login("+1000");

        verify(redis).put(eq("login-otp"), eq("+1000"), any(), anyLong());
        verify(sms).send(eq("+1000"), anyString());
    }

    @Test
    void validate_checksOtp_and_returnsToken_and_deletesOtp() {
        CacheStore redis = mock(CacheStore.class);
        when(redis.get("login-otp", "+2000")).thenReturn("111111");

        User user = new User();
        user.setId(5L);
        user.setPhone("+2000");

        UserDetailsService uds = mock(UserDetailsService.class);
        when(uds.loadUserByUsername("+2000")).thenReturn(user);

        JwtUtil jwtUtil = mock(JwtUtil.class);
        when(jwtUtil.generateToken(5L)).thenReturn("token-abc");

        SendSms sms = mock(SendSms.class);

        AuthService service = new AuthService(redis, uds, jwtUtil, sms);

        TokenResponse response = service.validate("+2000", "111111");
        assertThat(response.getAccessToken()).isEqualTo("token-abc");
        verify(redis).remove("login-otp", "+2000");
    }

    @Test
    void validate_throws_on_wrongOtp() {
        CacheStore redis = mock(CacheStore.class);
        when(redis.get("login-otp", "+2000")).thenReturn("111111");
        AuthService service = new AuthService(redis, mock(UserDetailsService.class), mock(JwtUtil.class), mock(SendSms.class));

        assertThatThrownBy(() -> service.validate("+2000", "000000"))
                .isInstanceOf(CerebraException.class)
                .hasMessageContaining("Invalid or expired OTP");
    }
}


