package com.altinntech.clicksave.core.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * The {@code EmbeddableClassData} class is a DTO used to store metadata about an embeddable entity class.
 *
 * <p>This class is used to cache metadata about entity classes.</p>
 *
 * @see ProjectionClassData
 *
 * @author Fyodor Plotnikov
 */
@Data
@NoArgsConstructor
public class EmbeddableClassData implements ClassData {

    private List<FieldDataCache> fields;

    @Override
    public void setIdField(FieldDataCache idField) {
    }
}

