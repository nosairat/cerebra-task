package sa.cerebra.task.cache.impl.map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({MockitoExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
@Execution(ExecutionMode.CONCURRENT)
class InMemoryCacheStoreTest {

    private InMemoryCacheStore cacheStore;

    private static final String CACHE_NAME = "testCache";
    private static final String KEY = "testKey";
    private static final String TEST_VALUE = "testValue";
    private static final long TIMEOUT_MINUTES = 1L;

    @BeforeEach
    void setUp() {
        cacheStore = new InMemoryCacheStore();
    }

    @Test
    void put_ShouldStoreValue_WhenValidParametersProvided() {
        // Given
        String expectedCacheKey = CACHE_NAME + ":" + KEY;

        // When
        cacheStore.put(CACHE_NAME, KEY, TEST_VALUE, TIMEOUT_MINUTES);

        // Then
        Object result = cacheStore.get(CACHE_NAME, KEY);
        assertEquals(TEST_VALUE, result);
    }





    @Test
    void put_ShouldHandleNullValue_WhenNullValueProvided() {
        // Given
        Object nullValue = null;

        // When
        cacheStore.put(CACHE_NAME, KEY, nullValue, TIMEOUT_MINUTES);

        // Then
        Object result = cacheStore.get(CACHE_NAME, KEY);
        assertNull(result);
    }

    @Test
    void put_ShouldHandleEmptyCacheName_WhenEmptyCacheNameProvided() {
        // Given
        String emptyCacheName = "";

        // When
        cacheStore.put(emptyCacheName, KEY, TEST_VALUE, TIMEOUT_MINUTES);

        // Then
        Object result = cacheStore.get(emptyCacheName, KEY);
        assertEquals(TEST_VALUE, result);
    }

    @Test
    void put_ShouldHandleEmptyKey_WhenEmptyKeyProvided() {
        // Given
        String emptyKey = "";

        // When
        cacheStore.put(CACHE_NAME, emptyKey, TEST_VALUE, TIMEOUT_MINUTES);

        // Then
        Object result = cacheStore.get(CACHE_NAME, emptyKey);
        assertEquals(TEST_VALUE, result);
    }

    @Test
    void put_ShouldHandleNullCacheName_WhenNullCacheNameProvided() {
        // Given
        String nullCacheName = null;

        // When
        cacheStore.put(nullCacheName, KEY, TEST_VALUE, TIMEOUT_MINUTES);

        // Then
        Object result = cacheStore.get(nullCacheName, KEY);
        assertEquals(TEST_VALUE, result);
    }

    @Test
    void put_ShouldHandleNullKey_WhenNullKeyProvided() {
        // Given
        String nullKey = null;

        // When
        cacheStore.put(CACHE_NAME, nullKey, TEST_VALUE, TIMEOUT_MINUTES);

        // Then
        Object result = cacheStore.get(CACHE_NAME, nullKey);
        assertEquals(TEST_VALUE, result);
    }

    @Test
    void get_ShouldReturnValue_WhenKeyExists() {
        // Given
        cacheStore.put(CACHE_NAME, KEY, TEST_VALUE, TIMEOUT_MINUTES);

        // When
        Object result = cacheStore.get(CACHE_NAME, KEY);

        // Then
        assertEquals(TEST_VALUE, result);
    }

    @Test
    void get_ShouldReturnNull_WhenKeyDoesNotExist() {
        // When
        Object result = cacheStore.get(CACHE_NAME, "nonExistentKey");

        // Then
        assertNull(result);
    }

    @Test
    void get_ShouldReturnNull_WhenValueIsNull() {
        // Given
        cacheStore.put(CACHE_NAME, KEY, null, TIMEOUT_MINUTES);

        // When
        Object result = cacheStore.get(CACHE_NAME, KEY);

        // Then
        assertNull(result);
    }





    @Test
    void remove_ShouldDeleteKey_WhenValidParametersProvided() {
        // Given
        cacheStore.put(CACHE_NAME, KEY, TEST_VALUE, TIMEOUT_MINUTES);
        assertNotNull(cacheStore.get(CACHE_NAME, KEY));

        // When
        cacheStore.remove(CACHE_NAME, KEY);

        // Then
        Object result = cacheStore.get(CACHE_NAME, KEY);
        assertNull(result);
    }

    @Test
    void remove_ShouldHandleEmptyCacheName_WhenEmptyCacheNameProvided() {
        // Given
        String emptyCacheName = "";
        cacheStore.put(emptyCacheName, KEY, TEST_VALUE, TIMEOUT_MINUTES);
        assertNotNull(cacheStore.get(emptyCacheName, KEY));

        // When
        cacheStore.remove(emptyCacheName, KEY);

        // Then
        Object result = cacheStore.get(emptyCacheName, KEY);
        assertNull(result);
    }

    @Test
    void remove_ShouldHandleEmptyKey_WhenEmptyKeyProvided() {
        // Given
        String emptyKey = "";
        cacheStore.put(CACHE_NAME, emptyKey, TEST_VALUE, TIMEOUT_MINUTES);
        assertNotNull(cacheStore.get(CACHE_NAME, emptyKey));

        // When
        cacheStore.remove(CACHE_NAME, emptyKey);

        // Then
        Object result = cacheStore.get(CACHE_NAME, emptyKey);
        assertNull(result);
    }

    @Test
    void remove_ShouldHandleNullCacheName_WhenNullCacheNameProvided() {
        // Given
        String nullCacheName = null;
        cacheStore.put(nullCacheName, KEY, TEST_VALUE, TIMEOUT_MINUTES);
        assertNotNull(cacheStore.get(nullCacheName, KEY));

        // When
        cacheStore.remove(nullCacheName, KEY);

        // Then
        Object result = cacheStore.get(nullCacheName, KEY);
        assertNull(result);
    }

    @Test
    void remove_ShouldHandleNullKey_WhenNullKeyProvided() {
        // Given
        String nullKey = null;
        cacheStore.put(CACHE_NAME, nullKey, TEST_VALUE, TIMEOUT_MINUTES);
        assertNotNull(cacheStore.get(CACHE_NAME, nullKey));

        // When
        cacheStore.remove(CACHE_NAME, nullKey);

        // Then
        Object result = cacheStore.get(CACHE_NAME, nullKey);
        assertNull(result);
    }

    @Test
    void put_ShouldHandleComplexObject_WhenComplexObjectProvided() {
        // Given
        TestComplexObject complexObject = new TestComplexObject("test", 123);

        // When
        cacheStore.put(CACHE_NAME, KEY, complexObject, TIMEOUT_MINUTES);

        // Then
        Object result = cacheStore.get(CACHE_NAME, KEY);
        assertEquals(complexObject, result);
    }

    @Test
    void get_ShouldReturnComplexObject_WhenComplexObjectStored() {
        // Given
        TestComplexObject complexObject = new TestComplexObject("test", 123);
        cacheStore.put(CACHE_NAME, KEY, complexObject, TIMEOUT_MINUTES);

        // When
        Object result = cacheStore.get(CACHE_NAME, KEY);

        // Then
        assertEquals(complexObject, result);
    }



    @Test
    void evictExpiredCacheEntries_ShouldNotRemoveNonExpiredEntries() {
        // Given
        cacheStore.put(CACHE_NAME, KEY, TEST_VALUE, TIMEOUT_MINUTES);

        // When
        cacheStore.evictExpiredCacheEntries();

        // Then
        assertEquals(TEST_VALUE, cacheStore.get(CACHE_NAME, KEY)); // Entry should still be there
    }

    @Test
    void evictExpiredCacheEntries_ShouldHandleEmptyCache() {
        // Given - empty cache

        // When
        cacheStore.evictExpiredCacheEntries();

        // Then - should not throw any exception
        assertDoesNotThrow(() -> cacheStore.evictExpiredCacheEntries());
    }

    @Test
    void put_ShouldOverwriteExistingValue_WhenSameKeyUsed() {
        // Given
        String originalValue = "original";
        String newValue = "new";
        cacheStore.put(CACHE_NAME, KEY, originalValue, TIMEOUT_MINUTES);
        assertEquals(originalValue, cacheStore.get(CACHE_NAME, KEY));

        // When
        cacheStore.put(CACHE_NAME, KEY, newValue, TIMEOUT_MINUTES);

        // Then
        assertEquals(newValue, cacheStore.get(CACHE_NAME, KEY));
    }

    @Test
    void put_ShouldHandleMultipleCacheNames_WhenDifferentCacheNamesUsed() {
        // Given
        String cacheName1 = "cache1";
        String cacheName2 = "cache2";
        String value1 = "value1";
        String value2 = "value2";

        // When
        cacheStore.put(cacheName1, KEY, value1, TIMEOUT_MINUTES);
        cacheStore.put(cacheName2, KEY, value2, TIMEOUT_MINUTES);

        // Then
        assertEquals(value1, cacheStore.get(cacheName1, KEY));
        assertEquals(value2, cacheStore.get(cacheName2, KEY));
    }

    @Test
    void put_ShouldHandleMultipleKeys_WhenDifferentKeysUsed() {
        // Given
        String key1 = "key1";
        String key2 = "key2";
        String value1 = "value1";
        String value2 = "value2";

        // When
        cacheStore.put(CACHE_NAME, key1, value1, TIMEOUT_MINUTES);
        cacheStore.put(CACHE_NAME, key2, value2, TIMEOUT_MINUTES);

        // Then
        assertEquals(value1, cacheStore.get(CACHE_NAME, key1));
        assertEquals(value2, cacheStore.get(CACHE_NAME, key2));
    }

    @Test
    @Execution(ExecutionMode.CONCURRENT)
    void put_ShouldBeThreadSafe_WhenMultipleThreadsAccessConcurrently() throws InterruptedException {
        // Given
        int numberOfThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        // When
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    cacheStore.put(CACHE_NAME, "key" + threadId, "value" + threadId, TIMEOUT_MINUTES);
                } finally {
                    latch.countDown();
                }
            });
        }

        // Then
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        executor.shutdown();

        // Verify all values were stored correctly
        for (int i = 0; i < numberOfThreads; i++) {
            assertEquals("value" + i, cacheStore.get(CACHE_NAME, "key" + i));
        }
    }

    @Test
    @Execution(ExecutionMode.CONCURRENT)
    void get_ShouldBeThreadSafe_WhenMultipleThreadsAccessConcurrently() throws InterruptedException {
        // Given
        cacheStore.put(CACHE_NAME, KEY, TEST_VALUE, TIMEOUT_MINUTES);
        int numberOfThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        // When
        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    assertEquals(TEST_VALUE, cacheStore.get(CACHE_NAME, KEY));
                } finally {
                    latch.countDown();
                }
            });
        }

        // Then
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        executor.shutdown();
    }

    @Test
    @Execution(ExecutionMode.CONCURRENT)
    void remove_ShouldBeThreadSafe_WhenMultipleThreadsAccessConcurrently() throws InterruptedException {
        // Given
        int numberOfThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        // When
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    cacheStore.put(CACHE_NAME, "key" + threadId, "value" + threadId, TIMEOUT_MINUTES);
                    cacheStore.remove(CACHE_NAME, "key" + threadId);
                } finally {
                    latch.countDown();
                }
            });
        }

        // Then
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        executor.shutdown();

        // Verify all values were removed
        for (int i = 0; i < numberOfThreads; i++) {
            assertNull(cacheStore.get(CACHE_NAME, "key" + i));
        }
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
