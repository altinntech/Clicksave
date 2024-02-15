package com.altinntech.clicksave.core.dto;

import com.altinntech.clicksave.annotations.Batching;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Optional;

@Data
@NoArgsConstructor
public class ClassDataCache {

    private String tableName;
    private List<FieldDataCache> fields;
    private FieldDataCache idField;
    private Batching batchingAnnotation;

    public Optional<Batching> getBatchingAnnotationOptional() {
        return Optional.ofNullable(batchingAnnotation);
    }
}
