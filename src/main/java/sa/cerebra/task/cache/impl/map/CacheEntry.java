package sa.cerebra.task.cache.impl.map;

import lombok.Getter;

import java.time.Instant;

class CacheEntry {
    @Getter
    private final Object data;
    private final Instant expiryTime;

    public CacheEntry(Object data, long timeoutInMinutes) {
        this.data = data;
        this.expiryTime = Instant.now().plusSeconds(timeoutInMinutes * 60);
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiryTime);
    }
}