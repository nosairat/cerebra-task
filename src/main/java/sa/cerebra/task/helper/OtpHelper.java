package sa.cerebra.task.helper;

import java.util.Random;


public class OtpHelper {
    private static final int OTP_LENGTH = 6;
    public static final int OTP_EXPIRY_MINUTES = 1;

    static public String generateRandomOtp() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(OTP_LENGTH);

        for (int i = 0; i < OTP_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }

        return sb.toString();
    }
}
