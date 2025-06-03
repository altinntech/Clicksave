package com.altinntech.clicksave.core.query.preprocessor;

import com.altinntech.clicksave.exceptions.ClassCacheNotFoundException;
import com.altinntech.clicksave.interfaces.QueryInfo;

public interface QueryPreprocessor {

    String preprocessQuery(QueryInfo queryInfo) throws ClassCacheNotFoundException;

}
