package com.altinntech.clicksave.core.caches;

import com.altinntech.clicksave.core.dto.QueryMetadata;

import java.util.HashMap;
import java.util.Map;

public class QueryMetadataCache {

    private static QueryMetadataCache instance;

    private final Map<String, QueryMetadata> queryMetadataCache = new HashMap<>();

    private QueryMetadataCache() {
    }

    public static QueryMetadataCache getInstance() {
        if (instance == null) {
            instance = new QueryMetadataCache();
        }
        return instance;
    }

    public void addToCache(String methodName, QueryMetadata QueryMetadata) {
        queryMetadataCache.put(methodName, QueryMetadata);
    }

    public QueryMetadata getFromCache(String methodName) {
        return queryMetadataCache.get(methodName);
    }

    public boolean containsKey(String methodName) {
        return queryMetadataCache.containsKey(methodName);
    }
}
