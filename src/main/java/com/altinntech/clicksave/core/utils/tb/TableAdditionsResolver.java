package com.altinntech.clicksave.core.utils.tb;

import com.altinntech.clicksave.annotations.OrderBy;
import com.altinntech.clicksave.annotations.PartitionBy;
import com.altinntech.clicksave.core.dto.ClassDataCache;
import com.altinntech.clicksave.enums.EngineType;
import com.altinntech.clicksave.enums.SystemField;

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

    public static void buildForMergeTree(StringBuilder query, ClassDataCache classDataCache, StringBuilder pk) {
        query.delete(query.length() - 2, query.length()).append(") ");
        query.append("ENGINE = ").append(EngineType.MergeTree);
        if (pk.length() > 0) {
            query.append(" PRIMARY KEY (").append(pk).append(")");
        }
        query.append(TableAdditionsResolver.getAdditions(classDataCache));
    }

    public static void buildForVersionedCollapsingMergeTree(StringBuilder query, ClassDataCache classDataCache, StringBuilder pk) {
        query.append(SystemField.Sign).append(", ").append(SystemField.Version).append(", ");
        query.delete(query.length() - 2, query.length()).append(") ");
        query.append("ENGINE = ").append(EngineType.VersionedCollapsingMergeTree)
                .append("(").append(SystemField.Sign.getName()).append(", ").append(SystemField.Version.getName()).append(")");
        if (pk.length() > 0) {
            query.append(" PRIMARY KEY (").append(pk).append(")");
        }
        query.append(TableAdditionsResolver.getAdditions(classDataCache));
    }

    public static void buildForMemoryEngine(StringBuilder query, ClassDataCache classDataCache, StringBuilder pk) {
        query.delete(query.length() - 2, query.length()).append(") ");
        query.append("ENGINE = ").append(EngineType.Memory);
    }

    public static void buildForBuffer(StringBuilder query) {
        query.delete(query.length() - 2, query.length()).append(") ");
        query.append("ENGINE = ").append(EngineType.Buffer);
        query.append("('', '', 16, 10, 100, 100, 1000, 1000000, 10000000);");
    }
}
