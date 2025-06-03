package com.altinntech.clicksave.core.query.preprocessor;

import com.altinntech.clicksave.core.caches.QueryMetadataCache;
import com.altinntech.clicksave.core.dto.CustomQueryMetadata;
import com.altinntech.clicksave.core.query.builder.QueryPullType;
import com.altinntech.clicksave.interfaces.QueryInfo;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class QueryAnnotationPreprocessor implements QueryPreprocessor {

    private final QueryMetadataCache queryMetadataCache;

    @Override
    public String preprocessQuery(QueryInfo queryInfo) {
        CustomQueryMetadata customQueryMetadata = new CustomQueryMetadata();
        customQueryMetadata.setQueryBody(queryInfo.queryString());
        customQueryMetadata.setPullType(QueryPullType.getByReturnType(queryInfo.containerClass()));
        customQueryMetadata.setIsQueryFromAnnotation(true);
        queryMetadataCache.addToCache(queryInfo.queryId(), customQueryMetadata);
        return queryInfo.queryId();
    }
}
