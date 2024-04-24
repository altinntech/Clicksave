package com.altinntech.clicksave;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.thepavel.icomponent.InterfaceComponentScan;


/**
 * The {@code ClickSaveConfiguration} class represents the configuration of the application.
 * It is annotated with Spring's {@code Configuration} annotation to indicate that it contains bean definitions.
 * It also uses {@code ComponentScan} to automatically register Spring beans within the specified packages.
 *
 * @author Fyodor Plotnikov
 */
@Configuration
@ComponentScan
@InterfaceComponentScan
public class ClickSaveConfiguration {
}
