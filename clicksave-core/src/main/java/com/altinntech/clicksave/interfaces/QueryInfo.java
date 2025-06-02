package com.altinntech.clicksave.interfaces;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public interface QueryInfo {

    String queryId();
    Type returnType();
    Annotation annotation();
}
