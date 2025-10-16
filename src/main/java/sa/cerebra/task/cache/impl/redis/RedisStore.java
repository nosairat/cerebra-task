package sa.cerebra.task.cache.impl.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import sa.cerebra.task.cache.CacheStore;

import java.util.concurrent.TimeUnit;

@Service
@Profile("default")
@RequiredArgsConstructor
public class RedisStore implements CacheStore {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void put(String cacheName, String key, Object data, long timeoutInMinutes) {
        redisTemplate.opsForValue().set(fromCacheKey(cacheName, key), data, timeoutInMinutes, TimeUnit.MINUTES);
        System.out.println("Set key: " + key);
    }

    @Override
    public Object get(String cacheName, String key) {
        // 'opsForValue().get(key)' retrieves the value
        Object value = redisTemplate.opsForValue().get(fromCacheKey(cacheName, key));
        System.out.println("Retrieved key: " + key + ", value: " + value);
        return value;
    }

    @Override
    public void remove(String cacheName, String key) {
        redisTemplate.delete(fromCacheKey(cacheName, key));
    }

}