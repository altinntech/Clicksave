package com.altinntech.clicksave.core.dto;

import com.altinntech.clicksave.core.query.builder.QueryPullType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CustomQueryMetadata implements QueryMetadata {

    String queryBody;
    QueryPullType pullType;
    List<FieldDataCache> fields;
}
