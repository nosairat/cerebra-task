package sa.cerebra.task.cache.impl.map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import sa.cerebra.task.cache.CacheStore;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


@Slf4j
@Profile("test,local")
@Service
public class InMemoryCacheStore implements CacheStore {
    public InMemoryCacheStore() {
    log.info("In InMemoryCacheStore");
    }

    // Using ConcurrentHashMap for thread-safe access
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    @Override
    public void put(String cacheName, String key, Object data, long timeoutInMinutes) {
        String fullKey = fromCacheKey(cacheName, key);
        CacheEntry entry = new CacheEntry(data, timeoutInMinutes);
        cache.put(fullKey, entry);
        System.out.println("Cached: " + fullKey + " with timeout: " + timeoutInMinutes + " min");
    }

    @Override
    public Object get(String cacheName, String key) {
        String fullKey = fromCacheKey(cacheName, key);
        CacheEntry entry = cache.get(fullKey);

        if (entry == null) {
            return null; // Cache miss
        }

        if (entry.isExpired()) {
            System.out.println("Cache Expired: " + fullKey + ". Removing entry.");
            cache.remove(fullKey); // Eagerly remove expired entry on access
            return null;
        }

        return entry.getData();
    }

    @Override
    public void remove(String cacheName, String key) {
        String fullKey = fromCacheKey(cacheName, key);
        cache.remove(fullKey);
    }

    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    public void evictExpiredCacheEntries() {
        log.info("--- Starting Scheduled Cache Cleanup ---");

        // Use an Iterator for safe removal while iterating over a ConcurrentHashMap
        Iterator<Map.Entry<String, CacheEntry>> iterator = cache.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, CacheEntry> entry = iterator.next();
            CacheEntry cacheEntry = entry.getValue();

            if (cacheEntry.isExpired()) {
                iterator.remove(); // Safely remove the entry
                log.debug("Evicted by Job: {}" , entry.getKey());
            }
        }

    }
}