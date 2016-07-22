package cz.uhk.cityunavigate.util;

import android.support.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple cache class for caching various network resources
 */
public class Cache<Key, Value> {

    private final Map<Key, CachedItem> cache = new HashMap<>();
    private final int limit;

    public Cache(int limit) {
        this.limit = limit;
    }

    public synchronized void cacheItem(@NotNull Key key, @NotNull Value value) {
        cache.put(key, new CachedItem(value, System.currentTimeMillis()));
        pruneCache();
    }

    public synchronized @Nullable Value getItem(@NotNull Key key) {
        CachedItem item = cache.get(key);
        if (item != null) {
            item.lastUsed = System.currentTimeMillis();
            return item.value;
        }
        return null;
    }

    private synchronized void pruneCache() {
        if (cache.size() <= limit)
            return;

        int removeCount = cache.size() - limit;
        List<Map.Entry<Key, CachedItem>> entries = new ArrayList<>(cache.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<Key, CachedItem>>() {
            @Override
            public int compare(Map.Entry<Key, CachedItem> e1, Map.Entry<Key, CachedItem> e2) {
                return (int)(e1.getValue().lastUsed - e2.getValue().lastUsed);
            }
        });

        for (int i = 0; i < removeCount; i++) {
            cache.remove(entries.get(i).getKey());
        }
    }

    private class CachedItem {
        Value value;
        long lastUsed;

        public CachedItem(Value value, long lastUsed) {
            this.value = value;
            this.lastUsed = lastUsed;
        }
    }
}
