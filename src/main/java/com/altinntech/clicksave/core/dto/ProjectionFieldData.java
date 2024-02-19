package com.altinntech.clicksave.core.dto;

import com.altinntech.clicksave.annotations.Reference;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;
import java.util.Optional;

@Data
@NoArgsConstructor
public class ProjectionFieldData implements FieldData {

    Field field;
    String fieldName;
    private String fieldInTableName;
    Reference referenceAnnotation;

    public Optional<Reference> getReferenceAnnotationOptional() {
        return Optional.ofNullable(referenceAnnotation);
    }
}
