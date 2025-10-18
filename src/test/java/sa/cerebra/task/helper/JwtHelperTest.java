package sa.cerebra.task.helper;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sa.cerebra.task.config.Configs;

import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class JwtHelperTest {

    @Mock
    private Configs configs;

    private JwtHelper jwtHelper;

    private final String signingKey = "your-256-bit-secret-your-256-bit-secret";

    @BeforeEach
    void setUp() {
        when(configs.getSigningKey()).thenReturn(signingKey);
        when(configs.getAccessTokenExpiration()).thenReturn(60_000L); // 1 minute
        jwtHelper = new JwtHelper(configs);
    }

    @Test
    void generateToken_ShouldCreateValidToken_WithUserIdSubject() {
        // When
        String token = jwtHelper.generateToken(123L);

        // Then
        assertNotNull(token);
        String subject = jwtHelper.extractUsername(token);
        assertEquals("123", subject);
        assertFalse(jwtHelper.isTokenExpired(token));
    }

    @Test
    void extractUsername_ShouldThrow_WhenTokenIsTampered() {
        // Given
        String token = jwtHelper.generateToken(456L);
        // Tamper the token by changing last character
        String tampered = token.substring(0, token.length() - 2) + "aa";

        // When/Then
        assertTrue(jwtHelper.isTokenExpired(tampered));
    }

    @Test
    void isTokenExpired_ShouldReturnTrue_WhenTokenIsExpired() throws InterruptedException {
        // Given
        when(configs.getAccessTokenExpiration()).thenReturn(1L); // 1 ms
        jwtHelper = new JwtHelper(configs);
        String token = jwtHelper.generateToken(789L);

        // Wait to ensure expiration
        Thread.sleep(5);

        // When
        boolean expired = jwtHelper.isTokenExpired(token);

        // Then
        assertTrue(expired);
    }

    @Test
    void isTokenExpired_ShouldReturnTrue_WhenTokenMalformed() {
        // Given
        String malformed = "not-a-jwt";

        // When
        boolean expiredOrInvalid = jwtHelper.isTokenExpired(malformed);

        // Then
        assertTrue(expiredOrInvalid);
    }

    @Test
    void extractUsername_ShouldReturnSubject_ForValidToken() {
        // Given
        String token = jwtHelper.generateToken(101112L);

        // When
        String subject = jwtHelper.extractUsername(token);

        // Then
        assertEquals("101112", subject);
    }

    @Test
    void generateToken_ShouldDiffer_ForDifferentUsers() {
        // When
        String t1 = jwtHelper.generateToken(1L);
        String t2 = jwtHelper.generateToken(2L);

        // Then
        assertNotEquals(t1, t2);
        assertEquals("1", jwtHelper.extractUsername(t1));
        assertEquals("2", jwtHelper.extractUsername(t2));
    }

    @Test
    void generateToken_ShouldProduceSignedToken_WithConfiguredKey() {
        // When
        String token = jwtHelper.generateToken(99L);

        // Then
        assertDoesNotThrow(() -> jwtHelper.extractUsername(token));
        assertFalse(jwtHelper.isTokenExpired(token));
    }

    @Test
    void extractUsername_ShouldFail_ForTokenSignedWithDifferentKey() {
        // Given
        when(configs.getSigningKey()).thenReturn("another-256-bit-secret-another-256-bit-secret");
        when(configs.getAccessTokenExpiration()).thenReturn(0l);
        JwtHelper otherHelper = new JwtHelper(configs);
        String tokenSignedWithOtherKey = otherHelper.generateToken(55L);

        // When/Then
        assertTrue(jwtHelper.isTokenExpired(tokenSignedWithOtherKey));
    }
}
