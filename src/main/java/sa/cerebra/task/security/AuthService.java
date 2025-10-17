package sa.cerebra.task.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import sa.cerebra.task.cache.CacheStore;
import sa.cerebra.task.dto.response.TokenResponse;
import sa.cerebra.task.entity.User;
import sa.cerebra.task.exception.CerebraException;
import sa.cerebra.task.exception.ErrorCode;
import sa.cerebra.task.service.SendSms;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    final CacheStore cacheStore;
    final UserDetailsService userDetailsService;
    final JwtUtil jwtUtil;
    final SendSms sendSms;
    private final String OTP_REDIS_NAME = "login-otp";

    public void login(String phone) {
//        todo add validation on phone number
        String otp = OtpHelper.generateRandomOtp();
        cacheStore.put(OTP_REDIS_NAME, phone, otp, OtpHelper.OTP_EXPIRY_MINUTES);

        log.debug("OTP generated for {}", otp);
        String otpMsg = String.format("This is the otp %s", otp);
        sendSms.send(phone, otpMsg);
    }

    public TokenResponse validate(String phone, String otp) {
        String storedOtp = (String) cacheStore.get(OTP_REDIS_NAME, phone);
        if (!otp.equals(storedOtp))
            throw new CerebraException(ErrorCode.INVALID_OTP);
        cacheStore.remove(OTP_REDIS_NAME, phone);
        User user = (User) userDetailsService.loadUserByUsername(phone);
        return TokenResponse.builder()
                .accessToken(jwtUtil.generateToken(user.getId()))
                .build();

    }

}
