package com.altinntech.clicksave.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The {@code BatchedQueryData} class represents data used for batched queries.
 * It stores the query body and information about the entity class related to the query.
 *
 * <p>This class is a DTO used to hold the query body and entity class information.</p>
 *
 * @author Fyodor Plotnikov
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchedQueryData {
    private String query;
    private ClassDataCache classDataCache;
}

