package sa.cerebra.task.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sa.cerebra.task.config.Configs;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTests {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() throws Exception {
        Configs configs = new Configs();
        // Configure a stable signing key and short expiration for tests
        setField(configs, "signingKey", "test-signing-key-256-bit-test-signing-key-256-bit");
        setField(configs, "accessTokenExpiration", 2000L);
        jwtUtil = new JwtUtil(configs);
    }

    @Test
    void generateToken_and_extractUsername_work() {
        String token = jwtUtil.generateToken(123L);
        assertThat(token).isNotBlank();
        String subject = jwtUtil.extractUsername(token);
        assertThat(subject).isEqualTo("123");
    }

    @Test
    void isTokenExpired_false_for_fresh_token_then_true_after_wait() throws InterruptedException {
        String token = jwtUtil.generateToken(1L);
        assertThat(jwtUtil.isTokenExpired(token)).isFalse();
        Thread.sleep(2000); // wait beyond 500ms expiration
        assertThat(jwtUtil.isTokenExpired(token)).isTrue();
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }
}


