package com.altinntech.clicksave;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static com.altinntech.clicksave.log.CSLogger.error;

/**
 * The {@code ClicksaveApplication} class represents the main entry point of the Clicksave application.
 * It is annotated with Spring Boot's {@code SpringBootApplication} annotation to enable autoconfiguration and component scanning.
 *
 * <p>Author: Fyodor Plotnikov</p>
 */
@SpringBootApplication
public class ClicksaveApplication {

    /**
     * The main method, the entry point of the application.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(ClicksaveApplication.class, args);
    }
}
