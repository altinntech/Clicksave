package com.altinntech.clicksave.annotations;

import org.springframework.stereotype.Service;
import org.thepavel.icomponent.Handler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@code ClickHouseRepository} annotation is used to mark interfaces as ClickHouse repositories.
 * It is intended to identify interfaces that serve as repositories for data access in ClickHouse.
 * This annotation also specifies the service and handler information related to the repository.
 *
 * @author Fyodor Plotnikov
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Service
@Handler("CSRequestHandler")
public @interface ClickHouseRepository {
}
