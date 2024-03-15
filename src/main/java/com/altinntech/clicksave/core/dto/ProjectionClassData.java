package com.altinntech.clicksave.core.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * The {@code ProjectionClassData} class is a DTO used to store information about projection classes.
 * It holds a list of projection field data.
 *
 * <p>This class is used to cache information about projection classes.</p>
 *
 * @see EmbeddableClassData
 *
 * @author Fyodor Plotnikov
 */
@Data
@NoArgsConstructor
public class ProjectionClassData {

    private List<FieldDataCache> fields;
}

