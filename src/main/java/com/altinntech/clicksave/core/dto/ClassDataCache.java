package com.altinntech.clicksave.core.dto;

import com.altinntech.clicksave.annotations.Batching;
import com.altinntech.clicksave.annotations.OrderBy;
import com.altinntech.clicksave.annotations.PartitionBy;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Optional;

/**
 * The {@code ClassDataCache} class is a DTO used to store metadata about an entity class.
 * It holds information such as the table name, fields, ID field, and batching annotation.
 *
 * <p>This class is used to cache metadata about entity classes.</p>
 *
 * @author Fyodor Plotnikov
 */
@Data
@NoArgsConstructor
public class ClassDataCache implements ClassData {

    private String tableName;
    private List<FieldDataCache> fields;
    private FieldDataCache idField;
    private Batching batchingAnnotation;
    private PartitionBy partitionByAnnotation;
    private OrderBy orderByAnnotation;

    /**
     * Retrieves the optional batching annotation associated with the class.
     *
     * @return the optional batching annotation
     */
    public Optional<Batching> getBatchingAnnotationOptional() {
        return Optional.ofNullable(batchingAnnotation);
    }

    /**
     * Retrieves the optional partitionBy annotation associated with the class.
     *
     * @return the optional partitionBy annotation
     */
    public Optional<PartitionBy> getPartitionByAnnotationOptional() {
        return Optional.ofNullable(partitionByAnnotation);
    }

    /**
     * Retrieves the optional orderBy annotation associated with the class.
     *
     * @return the optional orderBy annotation
     */
    public Optional<OrderBy> getOrderByAnnotationOptional() {
        return Optional.ofNullable(orderByAnnotation);
    }
}

