package com.altinntech.clicksave.core.query.preprocessor;

import com.altinntech.clicksave.core.ClassDataCacheService;
import com.altinntech.clicksave.core.caches.ProjectionClassDataCache;
import com.altinntech.clicksave.core.caches.QueryMetadataCache;
import com.altinntech.clicksave.core.dto.*;
import com.altinntech.clicksave.core.query.builder.QueryBuilder;
import com.altinntech.clicksave.core.query.builder.QueryPullType;
import com.altinntech.clicksave.core.query.parser.Part;
import com.altinntech.clicksave.core.query.parser.PartParser;
import com.altinntech.clicksave.exceptions.ClassCacheNotFoundException;
import com.altinntech.clicksave.interfaces.QueryInfo;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class QueryMethodNamePreprocessor implements QueryPreprocessor {

    private final QueryMetadataCache metadataCache;
    private final ClassDataCacheService classDataCacheService;

    @Override
    public String preprocessQuery(QueryInfo queryInfo) throws ClassCacheNotFoundException {
        PartParser partParser = new PartParser();
        partParser.parse(queryInfo.methodName());
        List<Part> parts = partParser.getParts();

        ClassDataCache classDataCache = classDataCacheService.getClassDataCache(queryInfo.entityClass());
        List<FieldDataCache> fieldDataCacheList = classDataCache.getFields();
        List<FieldData> fieldsToFetch;

        if (!queryInfo.returnClass().equals(queryInfo.entityClass())) {
            ProjectionClassData projectionClassData = ProjectionClassDataCache.getInstance().get(queryInfo.returnClass());
            fieldsToFetch = new ArrayList<>(projectionClassData.getFields());
        } else {
            fieldsToFetch = getFieldsToFetch(fieldDataCacheList);
        }

        QueryBuilder queryBuilder = new QueryBuilder(parts, classDataCache.getTableName(), fieldDataCacheList, fieldsToFetch);
        CustomQueryMetadata query = queryBuilder.createQuery();
        query.setPullType(QueryPullType.getByReturnType(queryInfo.containerClass()));
        query.setIsQueryFromAnnotation(false);
        metadataCache.addToCache(queryInfo.queryId(), query);
        return queryInfo.queryId();
    }

    private List<FieldData> getFieldsToFetch(List<FieldDataCache> fieldDataCacheList) throws ClassCacheNotFoundException {
        List<FieldData> result = new ArrayList<>();
        for (FieldDataCache fieldData : fieldDataCacheList) {
            if (fieldData.getEmbeddedAnnotation().isPresent()) {
                EmbeddableClassData embeddableClassData = classDataCacheService.getEmbeddableClassDataCache(fieldData.getType());
                result.addAll(getFieldsToFetch(embeddableClassData.getFields()));
            } else {
                result.add(fieldData);
            }
        }
        return result;
    }
}
