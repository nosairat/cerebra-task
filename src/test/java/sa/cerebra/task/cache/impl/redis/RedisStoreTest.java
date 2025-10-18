package sa.cerebra.task.cache.impl.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class RedisStoreTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private RedisStore redisStore;

    private static final String CACHE_NAME = "testCache";
    private static final String KEY = "testKey";
    private static final String TEST_VALUE = "testValue";
    private static final long TIMEOUT_MINUTES = 30L;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void put_ShouldSetValueWithTimeout_WhenValidParametersProvided() {
        // Given
        String expectedCacheKey = CACHE_NAME + ":" + KEY;

        // When
        redisStore.put(CACHE_NAME, KEY, TEST_VALUE, TIMEOUT_MINUTES);

        // Then
        verify(valueOperations).set(eq(expectedCacheKey), eq(TEST_VALUE), eq(TIMEOUT_MINUTES), eq(TimeUnit.MINUTES));
    }

    @Test
    void put_ShouldSetValueWithZeroTimeout_WhenZeroTimeoutProvided() {
        // Given
        String expectedCacheKey = CACHE_NAME + ":" + KEY;
        long zeroTimeout = 0L;

        // When
        redisStore.put(CACHE_NAME, KEY, TEST_VALUE, zeroTimeout);

        // Then
        verify(valueOperations).set(eq(expectedCacheKey), eq(TEST_VALUE), eq(zeroTimeout), eq(TimeUnit.MINUTES));
    }

    @Test
    void put_ShouldSetValueWithNegativeTimeout_WhenNegativeTimeoutProvided() {
        // Given
        String expectedCacheKey = CACHE_NAME + ":" + KEY;
        long negativeTimeout = -1L;

        // When
        redisStore.put(CACHE_NAME, KEY, TEST_VALUE, negativeTimeout);

        // Then
        verify(valueOperations).set(eq(expectedCacheKey), eq(TEST_VALUE), eq(negativeTimeout), eq(TimeUnit.MINUTES));
    }

    @Test
    void put_ShouldHandleNullValue_WhenNullValueProvided() {
        // Given
        String expectedCacheKey = CACHE_NAME + ":" + KEY;
        Object nullValue = null;

        // When
        redisStore.put(CACHE_NAME, KEY, nullValue, TIMEOUT_MINUTES);

        // Then
        verify(valueOperations).set(eq(expectedCacheKey), eq(nullValue), eq(TIMEOUT_MINUTES), eq(TimeUnit.MINUTES));
    }

    @Test
    void put_ShouldHandleEmptyCacheName_WhenEmptyCacheNameProvided() {
        // Given
        String emptyCacheName = "";
        String expectedCacheKey = ":" + KEY;

        // When
        redisStore.put(emptyCacheName, KEY, TEST_VALUE, TIMEOUT_MINUTES);

        // Then
        verify(valueOperations).set(eq(expectedCacheKey), eq(TEST_VALUE), eq(TIMEOUT_MINUTES), eq(TimeUnit.MINUTES));
    }

    @Test
    void put_ShouldHandleEmptyKey_WhenEmptyKeyProvided() {
        // Given
        String emptyKey = "";
        String expectedCacheKey = CACHE_NAME + ":";

        // When
        redisStore.put(CACHE_NAME, emptyKey, TEST_VALUE, TIMEOUT_MINUTES);

        // Then
        verify(valueOperations).set(eq(expectedCacheKey), eq(TEST_VALUE), eq(TIMEOUT_MINUTES), eq(TimeUnit.MINUTES));
    }

    @Test
    void get_ShouldReturnValue_WhenKeyExists() {
        // Given
        String expectedCacheKey = CACHE_NAME + ":" + KEY;
        when(valueOperations.get(expectedCacheKey)).thenReturn(TEST_VALUE);

        // When
        Object result = redisStore.get(CACHE_NAME, KEY);

        // Then
        assertEquals(TEST_VALUE, result);
        verify(valueOperations).get(expectedCacheKey);
    }

    @Test
    void get_ShouldReturnNull_WhenKeyDoesNotExist() {
        // Given
        String expectedCacheKey = CACHE_NAME + ":" + KEY;
        when(valueOperations.get(expectedCacheKey)).thenReturn(null);

        // When
        Object result = redisStore.get(CACHE_NAME, KEY);

        // Then
        assertNull(result);
        verify(valueOperations).get(expectedCacheKey);
    }

    @Test
    void get_ShouldReturnNull_WhenValueIsNull() {
        // Given
        String expectedCacheKey = CACHE_NAME + ":" + KEY;
        when(valueOperations.get(expectedCacheKey)).thenReturn(null);

        // When
        Object result = redisStore.get(CACHE_NAME, KEY);

        // Then
        assertNull(result);
        verify(valueOperations).get(expectedCacheKey);
    }

    @Test
    void get_ShouldHandleEmptyCacheName_WhenEmptyCacheNameProvided() {
        // Given
        String emptyCacheName = "";
        String expectedCacheKey = ":" + KEY;
        when(valueOperations.get(expectedCacheKey)).thenReturn(TEST_VALUE);

        // When
        Object result = redisStore.get(emptyCacheName, KEY);

        // Then
        assertEquals(TEST_VALUE, result);
        verify(valueOperations).get(expectedCacheKey);
    }

    @Test
    void get_ShouldHandleEmptyKey_WhenEmptyKeyProvided() {
        // Given
        String emptyKey = "";
        String expectedCacheKey = CACHE_NAME + ":";
        when(valueOperations.get(expectedCacheKey)).thenReturn(TEST_VALUE);

        // When
        Object result = redisStore.get(CACHE_NAME, emptyKey);

        // Then
        assertEquals(TEST_VALUE, result);
        verify(valueOperations).get(expectedCacheKey);
    }

    @Test
    void remove_ShouldDeleteKey_WhenValidParametersProvided() {
        // Given
        String expectedCacheKey = CACHE_NAME + ":" + KEY;

        // When
        redisStore.remove(CACHE_NAME, KEY);

        // Then
        verify(redisTemplate).delete(expectedCacheKey);
    }

    @Test
    void remove_ShouldHandleEmptyCacheName_WhenEmptyCacheNameProvided() {
        // Given
        String emptyCacheName = "";
        String expectedCacheKey = ":" + KEY;

        // When
        redisStore.remove(emptyCacheName, KEY);

        // Then
        verify(redisTemplate).delete(expectedCacheKey);
    }

    @Test
    void remove_ShouldHandleEmptyKey_WhenEmptyKeyProvided() {
        // Given
        String emptyKey = "";
        String expectedCacheKey = CACHE_NAME + ":";

        // When
        redisStore.remove(CACHE_NAME, emptyKey);

        // Then
        verify(redisTemplate).delete(expectedCacheKey);
    }

    @Test
    void remove_ShouldHandleNullCacheName_WhenNullCacheNameProvided() {
        // Given
        String nullCacheName = null;
        String expectedCacheKey = "null:" + KEY;

        // When
        redisStore.remove(nullCacheName, KEY);

        // Then
        verify(redisTemplate).delete(expectedCacheKey);
    }

    @Test
    void remove_ShouldHandleNullKey_WhenNullKeyProvided() {
        // Given
        String nullKey = null;
        String expectedCacheKey = CACHE_NAME + ":null";

        // When
        redisStore.remove(CACHE_NAME, nullKey);

        // Then
        verify(redisTemplate).delete(expectedCacheKey);
    }

    @Test
    void put_ShouldHandleNullCacheName_WhenNullCacheNameProvided() {
        // Given
        String nullCacheName = null;
        String expectedCacheKey = "null:" + KEY;

        // When
        redisStore.put(nullCacheName, KEY, TEST_VALUE, TIMEOUT_MINUTES);

        // Then
        verify(valueOperations).set(eq(expectedCacheKey), eq(TEST_VALUE), eq(TIMEOUT_MINUTES), eq(TimeUnit.MINUTES));
    }

    @Test
    void put_ShouldHandleNullKey_WhenNullKeyProvided() {
        // Given
        String nullKey = null;
        String expectedCacheKey = CACHE_NAME + ":null";

        // When
        redisStore.put(CACHE_NAME, nullKey, TEST_VALUE, TIMEOUT_MINUTES);

        // Then
        verify(valueOperations).set(eq(expectedCacheKey), eq(TEST_VALUE), eq(TIMEOUT_MINUTES), eq(TimeUnit.MINUTES));
    }

    @Test
    void get_ShouldHandleNullCacheName_WhenNullCacheNameProvided() {
        // Given
        String nullCacheName = null;
        String expectedCacheKey = "null:" + KEY;
        when(valueOperations.get(expectedCacheKey)).thenReturn(TEST_VALUE);

        // When
        Object result = redisStore.get(nullCacheName, KEY);

        // Then
        assertEquals(TEST_VALUE, result);
        verify(valueOperations).get(expectedCacheKey);
    }

    @Test
    void get_ShouldHandleNullKey_WhenNullKeyProvided() {
        // Given
        String nullKey = null;
        String expectedCacheKey = CACHE_NAME + ":null";
        when(valueOperations.get(expectedCacheKey)).thenReturn(TEST_VALUE);

        // When
        Object result = redisStore.get(CACHE_NAME, nullKey);

        // Then
        assertEquals(TEST_VALUE, result);
        verify(valueOperations).get(expectedCacheKey);
    }

    @Test
    void put_ShouldHandleComplexObject_WhenComplexObjectProvided() {
        // Given
        String expectedCacheKey = CACHE_NAME + ":" + KEY;
        Object complexObject = new TestComplexObject("test", 123);

        // When
        redisStore.put(CACHE_NAME, KEY, complexObject, TIMEOUT_MINUTES);

        // Then
        verify(valueOperations).set(eq(expectedCacheKey), eq(complexObject), eq(TIMEOUT_MINUTES), eq(TimeUnit.MINUTES));
    }

    @Test
    void get_ShouldReturnComplexObject_WhenComplexObjectStored() {
        // Given
        String expectedCacheKey = CACHE_NAME + ":" + KEY;
        TestComplexObject complexObject = new TestComplexObject("test", 123);
        when(valueOperations.get(expectedCacheKey)).thenReturn(complexObject);

        // When
        Object result = redisStore.get(CACHE_NAME, KEY);

        // Then
        assertEquals(complexObject, result);
        verify(valueOperations).get(expectedCacheKey);
    }

    // Helper class for testing complex objects
    private static class TestComplexObject {
        private final String name;
        private final int value;

        public TestComplexObject(String name, int value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            TestComplexObject that = (TestComplexObject) obj;
            return value == that.value && name.equals(that.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode() + value;
        }
    }
}
