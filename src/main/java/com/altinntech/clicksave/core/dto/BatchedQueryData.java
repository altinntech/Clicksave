package com.altinntech.clicksave.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchedQueryData {
    private String query;
    private ClassDataCache classDataCache;
}
