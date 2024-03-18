package com.altinntech.clicksave.core.utils;

import com.altinntech.clicksave.annotations.OrderBy;
import com.altinntech.clicksave.annotations.PartitionBy;
import com.altinntech.clicksave.core.dto.ClassDataCache;

import java.util.Optional;

public class TableAdditionsResolver {

    public static String getAdditions(ClassDataCache classDataCache) {
        StringBuilder result = new StringBuilder();
        Optional<OrderBy> orderBy = classDataCache.getOrderByAnnotationOptional();
        Optional<PartitionBy> partitionBy = classDataCache.getPartitionByAnnotationOptional();
        orderBy.ifPresent(by -> result.append(" ORDER BY ").append(by.value()));
        partitionBy.ifPresent(by -> result.append(" PARTITION BY ").append(by.value()));
        return result.toString();
    }
}
