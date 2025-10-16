package sa.cerebra.task.cache;

public interface CacheStore {
    void put(String cacheName, String key, Object data, long timeoutInMinutes);

    Object get(String cacheName, String key);

    //    // --- DELETE Operation ---
    void remove(String cacheName, String key);

    default String fromCacheKey(String cacheName, String key) {
        return cacheName + ":" + key;
    }

}
