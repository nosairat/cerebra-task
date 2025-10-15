package sa.cerebra.task.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisStore {

    private final RedisTemplate<String, Object> redisTemplate;

    // --- SET/PUT Operation ---
    // The "template" is used for generic key-value operations
    public void cacheData(String cacheName,String key, Object data, long timeoutInMinutes) {
        // 'opsForValue()' gives you access to simple String/Object operations
        redisTemplate.opsForValue().set(fromCacheKey(cacheName, key), data, timeoutInMinutes, TimeUnit.MINUTES);
        System.out.println("Set key: " + key);
    }

    // --- GET Operation ---
    public Object retrieveData(String cacheName,String key) {
        // 'opsForValue().get(key)' retrieves the value
        Object value = redisTemplate.opsForValue().get(fromCacheKey(cacheName, key));
        System.out.println("Retrieved key: " + key + ", value: " + value);
        return value;
    }

//    // --- DELETE Operation ---
    public Boolean deleteData(String cacheName,String key) {
        return redisTemplate.delete(fromCacheKey(cacheName, key));
    }

    private static String fromCacheKey(String cacheName, String key) {
        return cacheName + ":" + key;
    }
}