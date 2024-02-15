package com.altinntech.clicksave.core.query.builder;

import com.altinntech.clicksave.core.dto.FieldDataCache;
import com.altinntech.clicksave.core.dto.CustomQueryMetadata;
import com.altinntech.clicksave.core.query.parser.CommonPart;
import com.altinntech.clicksave.core.query.parser.FieldPart;
import com.altinntech.clicksave.core.query.parser.Part;
import com.altinntech.clicksave.exceptions.WrongQueryMethodException;

import java.util.ArrayList;
import java.util.List;

import static com.altinntech.clicksave.core.query.builder.SqlPartDefinition.sqlPartsMap;

public class QueryBuilder {

    private final StringBuilder preparedQuery = new StringBuilder();
    private final List<Part> parts;
    private final String tableName;
    private final List<FieldDataCache> fieldsData;
    private final List<FieldDataCache> queriedFieldsData = new ArrayList<>();

    private CommonPart qualifierPart;
    private QueryPullType pullType = QueryPullType.NONE;

    public QueryBuilder(List<Part> parts, String tableName, List<FieldDataCache> fieldsData) {
        this.parts = parts;
        this.tableName = tableName;
        this.fieldsData = fieldsData;
    }

    public CustomQueryMetadata createQuery() {
        qualifierPart = (CommonPart) parts.getFirst();
        QueryType queryType = qualifierPart.getQualifier();
        if (qualifierPart.equals(CommonPart.FIND_ALL_BY_PART))
            pullType = QueryPullType.MULTIPLE;
        else if (qualifierPart.equals(CommonPart.FIND_BY_PART))
            pullType = QueryPullType.SINGLE;
        switch (queryType) {
            case SELECT -> buildSelectQuery();
            case DELETE -> {
                return new CustomQueryMetadata(); // todo: no implementation
            }
        }
        CustomQueryMetadata customQueryMetadata = new CustomQueryMetadata();
        customQueryMetadata.setQueryBody(preparedQuery.toString());
        customQueryMetadata.setPullType(pullType);
        customQueryMetadata.setFields(queriedFieldsData);
        return customQueryMetadata;
    }

    private void buildSelectQuery() {
        preparedQuery.append(sqlPartsMap.get(qualifierPart)).append(" FROM ").append(tableName).append(" WHERE ");
        for (Part part : parts) {
            if (part instanceof CommonPart commonPart && !commonPart.equals(qualifierPart)) {
                preparedQuery.append(sqlPartsMap.get(commonPart)).append(" ");
            } else if (part instanceof FieldPart) {

                FieldDataCache fieldData = fieldsData.stream()
                        .filter(fd -> fd.getFieldName().equalsIgnoreCase(part.getPartName()))
                        .findAny()
                        .orElseThrow(() -> new WrongQueryMethodException("Field not found: " + part.getPartName()));

                preparedQuery.append(fieldData.getFieldInTableName()).append(" = ?").append(" ");
                queriedFieldsData.add(fieldData);
            }
        }
        preparedQuery.deleteCharAt(preparedQuery.length() - 1);
    }
}
