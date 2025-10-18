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

    @Test
    @DisplayName("Should generate OTP with leading zeros when needed")
    void generateRandomOtp_ShouldGenerateOtpWithLeadingZerosWhenNeeded() {
        // Given
        boolean foundLeadingZero = false;
        int attempts = 1000;

        // When
        for (int i = 0; i < attempts; i++) {
            String otp = OtpHelper.generateRandomOtp();
            if (otp.startsWith("0")) {
                foundLeadingZero = true;
                break;
            }
        }

        // Then
        assertTrue(foundLeadingZero, "Should generate OTPs with leading zeros");
    }

    @Test
    @DisplayName("Should generate OTP with trailing zeros when needed")
    void generateRandomOtp_ShouldGenerateOtpWithTrailingZerosWhenNeeded() {
        // Given
        boolean foundTrailingZero = false;
        int attempts = 1000;

        // When
        for (int i = 0; i < attempts; i++) {
            String otp = OtpHelper.generateRandomOtp();
            if (otp.endsWith("0")) {
                foundTrailingZero = true;
                break;
            }
        }

        // Then
        assertTrue(foundTrailingZero, "Should generate OTPs with trailing zeros");
    }

    @Test
    @DisplayName("Should generate OTP with middle zeros when needed")
    void generateRandomOtp_ShouldGenerateOtpWithMiddleZerosWhenNeeded() {
        // Given
        boolean foundMiddleZero = false;
        int attempts = 1000;

        // When
        for (int i = 0; i < attempts; i++) {
            String otp = OtpHelper.generateRandomOtp();
            if (otp.contains("0") && !otp.startsWith("0") && !otp.endsWith("0")) {
                foundMiddleZero = true;
                break;
            }
        }

        // Then
        assertTrue(foundMiddleZero, "Should generate OTPs with zeros in the middle");
    }


    @Test
    @DisplayName("Should generate OTP with palindrome pattern when needed")
    void generateRandomOtp_ShouldGenerateOtpWithPalindromePatternWhenNeeded() {
        // Given
        boolean foundPalindrome = false;
        int attempts = 10000; // Increased attempts for this rare case

        // When
        for (int i = 0; i < attempts; i++) {
            String otp = OtpHelper.generateRandomOtp();
            if (otp.equals(new StringBuilder(otp).reverse().toString())) {
                foundPalindrome = true;
                break;
            }
        }

        // Then
        assertTrue(foundPalindrome, "Should generate OTPs with palindrome pattern");
    }


    @Test
    @DisplayName("Should generate OTP with maximum value when needed")
    void generateRandomOtp_ShouldGenerateOtpWithMaximumValueWhenNeeded() {
        // Given
        boolean foundMaxValue = false;
        int attempts = 1000000; // Very high attempts for this extremely rare case

        // When
        for (int i = 0; i < attempts; i++) {
            String otp = OtpHelper.generateRandomOtp();
            if (otp.equals("999999")) {
                foundMaxValue = true;
                break;
            }
        }

        // Then
        assertTrue(foundMaxValue, "Should generate OTP with maximum value 999999");
    }


}
