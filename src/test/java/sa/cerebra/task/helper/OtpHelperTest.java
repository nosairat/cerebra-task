package sa.cerebra.task.helper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class OtpHelperTest {

    @Test
    @DisplayName("Should generate OTP with correct length")
    void generateRandomOtp_ShouldGenerateOtpWithCorrectLength() {
        // When
        String otp = OtpHelper.generateRandomOtp();

        // Then
        assertEquals(6, otp.length());
    }

    @Test
    @DisplayName("Should generate OTP containing only digits")
    void generateRandomOtp_ShouldGenerateOtpWithOnlyDigits() {
        // When
        String otp = OtpHelper.generateRandomOtp();

        // Then
        assertTrue(otp.matches("\\d{6}"), "OTP should contain only digits, but was: " + otp);
    }

    @Test
    @DisplayName("Should generate different OTPs on multiple calls")
    void generateRandomOtp_ShouldGenerateDifferentOtpsOnMultipleCalls() {
        // When
        String otp1 = OtpHelper.generateRandomOtp();
        String otp2 = OtpHelper.generateRandomOtp();
        String otp3 = OtpHelper.generateRandomOtp();

        // Then
        assertNotEquals(otp1, otp2, "OTPs should be different");
        assertNotEquals(otp2, otp3, "OTPs should be different");
        assertNotEquals(otp1, otp3, "OTPs should be different");
    }

    @RepeatedTest(100)
    @DisplayName("Should generate valid OTP format consistently")
    void generateRandomOtp_ShouldGenerateValidOtpFormatConsistently() {
        // When
        String otp = OtpHelper.generateRandomOtp();

        // Then
        assertNotNull(otp, "OTP should not be null");
        assertEquals(6, otp.length(), "OTP should have length 6");
        assertTrue(otp.matches("\\d{6}"), "OTP should contain only digits");
        
        // Verify each character is a digit
        for (char c : otp.toCharArray()) {
            assertTrue(Character.isDigit(c), "Each character should be a digit");
        }
    }

    @Test
    @DisplayName("Should generate OTP with all possible digits")
    void generateRandomOtp_ShouldGenerateOtpWithAllPossibleDigits() {
        // Given
        boolean[] digitsFound = new boolean[10];
        int attempts = 1000;

        // When
        for (int i = 0; i < attempts; i++) {
            String otp = OtpHelper.generateRandomOtp();
            for (char c : otp.toCharArray()) {
                int digit = Character.getNumericValue(c);
                digitsFound[digit] = true;
            }
        }

        // Then
        for (int i = 0; i < 10; i++) {
            assertTrue(digitsFound[i], "Digit " + i + " should appear in generated OTPs");
        }
    }



    @Test
    @DisplayName("Should generate OTP that can be parsed as integer")
    void generateRandomOtp_ShouldGenerateOtpThatCanBeParsedAsInteger() {
        // When
        String otp = OtpHelper.generateRandomOtp();

        // Then
        assertDoesNotThrow(() -> Integer.parseInt(otp), "OTP should be parseable as integer");
        
        int otpValue = Integer.parseInt(otp);
        assertTrue(otpValue >= 0, "OTP value should be non-negative");
        assertTrue(otpValue <= 999999, "OTP value should not exceed 6 digits");
    }


}
