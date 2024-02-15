package com.altinntech.clicksave.annotations;

import org.springframework.stereotype.Service;
import org.thepavel.icomponent.Handler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Service
@Handler("CSRequestHandler")
public @interface ClickHouseRepository {
}
