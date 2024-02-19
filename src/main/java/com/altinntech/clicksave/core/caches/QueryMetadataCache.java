package com.altinntech.clicksave.core.caches;

import com.altinntech.clicksave.core.dto.QueryMetadata;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@code QueryMetadataCache} class is used to cache query metadata.
 * It stores information about queries and their metadata.
 *
 * @author Fyodor Plotnikov
 */
public class QueryMetadataCache {

    private static QueryMetadataCache instance;

    private final Map<String, QueryMetadata> queryMetadataCache = new HashMap<>();

    private QueryMetadataCache() {
    }

    /**
     * Retrieves the instance of QueryMetadataCache.
     *
     * @return the instance of QueryMetadataCache
     */
    public static QueryMetadataCache getInstance() {
        if (instance == null) {
            instance = new QueryMetadataCache();
        }
        return instance;
    }

    /**
     * Adds query metadata to the cache.
     *
     * @param methodName    the method name
     * @param queryMetadata the query metadata
     */
    public void addToCache(String methodName, QueryMetadata queryMetadata) {
        queryMetadataCache.put(methodName, queryMetadata);
    }

    /**
     * Retrieves query metadata from the cache.
     *
     * @param methodName the method name
     * @return the query metadata retrieved from the cache
     */
    public QueryMetadata getFromCache(String methodName) {
        return queryMetadataCache.get(methodName);
    }

    /**
     * Checks if the cache contains the specified key (method name).
     *
     * @param methodName the method name
     * @return {@code true} if the cache contains the key, {@code false} otherwise
     */
    public boolean containsKey(String methodName) {
        return queryMetadataCache.containsKey(methodName);
    }
}
