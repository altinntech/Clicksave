package com.altinntech.clicksave.core.dto;

import com.altinntech.clicksave.core.query.builder.QueryPullType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * The {@code CustomQueryMetadata} class represents metadata for custom queries.
 * It holds information about the query body, pull type, and fields.
 *
 * <p>This class implements the {@code QueryMetadata} interface.</p>
 *
 * <p>The {@code pullType} indicates the entity retrieval mode: single or multiple.</p>
 *
 * <p>This DTO is used to store information about custom queries.</p>
 *
 * @author Fyodor Plotnikov
 */
@Data
@NoArgsConstructor
public class CustomQueryMetadata implements QueryMetadata {

    /**
     * The query body.
     */
    String queryBody;

    /**
     * The pull type indicating entity retrieval mode: single or multiple.
     */
    QueryPullType pullType;

    /**
     * The list of fields.
     */
    List<FieldDataCache> fields;

    /**
     * Is by annotation.
     */
    Boolean isQueryFromAnnotation = false;
}

