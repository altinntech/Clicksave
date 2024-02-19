package com.altinntech.clicksave.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchedQueryData {
    private String query;
    private ClassDataCache classDataCache;
}
