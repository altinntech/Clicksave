package com.altinntech.clicksave.core.pipelines.insert;

import com.altinntech.clicksave.enums.EngineType;

public class InsertQueryBuilderFactory {

    public static InsertQueryBuilder getQueryBuilder(EngineType engineType) {
        return switch (engineType) {
            case VersionedCollapsingMergeTree -> new VersionedCollapsingMergeTreeQueryBuilder();
            case MergeTree -> new MergeTreeQueryBuilder();
            case ReplacingMergeTree -> new ReplacingMergeTreeQueryBuilder();
            case Buffer -> new BufferQueryBuilder();
            default -> throw new UnsupportedOperationException("Unsupported engine type: " + engineType);
        };
    }
}
