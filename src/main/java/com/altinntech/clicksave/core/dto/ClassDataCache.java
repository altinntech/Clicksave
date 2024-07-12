package com.altinntech.clicksave.core.dto;

import com.altinntech.clicksave.annotations.*;
import com.altinntech.clicksave.enums.EngineType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
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
public class ClassDataCache implements ClassData, Serializable {

    @Serial
    private static final long serialVersionUID = 87995945L;

    private Class<?> entityClass;
    private ClickHouseEntity CHEAnnotation;
    private String tableName;
    private List<FieldDataCache> fields;
    private MethodDataCache methodData;
    private FieldDataCache idField;
    private EngineType engineType;
    private Batching batchingAnnotation;
    private PartitionBy partitionByAnnotation;
    private OrderBy orderByAnnotation;
    private SystemTable systemTableAnnotation;

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

    /**
     * Retrieves the optional system table annotation associated with the class.
     *
     * @return the optional system table annotation
     */
    public Optional<SystemTable> getSystemTableAnnotationOptional() {
        return Optional.ofNullable(systemTableAnnotation);
    }

    /**
     * Retrieves the optional CHE annotation associated with the class.
     *
     * @return the optional CHE annotation
     */
    public Optional<ClickHouseEntity> getCHEAnnotationOptional() {
        return Optional.ofNullable(CHEAnnotation);
    }

    public void setCHEAnnotation(ClickHouseEntity annotation) {
        if (Objects.nonNull(annotation)) {
            this.engineType = annotation.engine();
        } else {
            this.engineType = EngineType.MergeTree;
        }
        this.CHEAnnotation = annotation;
    }
}

