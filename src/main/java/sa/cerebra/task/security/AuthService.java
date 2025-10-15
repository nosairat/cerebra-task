package sa.cerebra.task.security;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import sa.cerebra.task.redis.RedisStore;
import sa.cerebra.task.dto.response.TokenResponse;
import sa.cerebra.task.entity.User;
import sa.cerebra.task.exception.CerebraException;
import sa.cerebra.task.repository.UserRepository;
import sa.cerebra.task.service.SendSms;

@Service
@RequiredArgsConstructor
public class AuthService {
    final RedisStore redisStore;
    final UserDetailsService userDetailsService;
    final JwtUtil jwtUtil;
    final SendSms sendSms;
    private final String OTP_REDIS_NAME = "login-otp";

    public void login(String phone) {
//        todo add validation on phone number
//        String otp = OtpHelper.generateRandomOtp();
        String otp = "111111";
        redisStore.cacheData(OTP_REDIS_NAME, phone, otp, OtpHelper.OTP_EXPIRY_MINUTES);

        sendSms.send(phone, otp);
    }

    public TokenResponse validate(String phone, String otp) {
        String storedOtp = (String) redisStore.retrieveData(OTP_REDIS_NAME, phone);
        if (!otp.equals(storedOtp))
            throw new CerebraException("Invalid or expired OTP");
        redisStore.deleteData(OTP_REDIS_NAME, phone);
        User user = (User) userDetailsService.loadUserByUsername(phone);
        return TokenResponse.builder()
                .accessToken(jwtUtil.generateToken(user.getId()))
                .build();

    }

}
