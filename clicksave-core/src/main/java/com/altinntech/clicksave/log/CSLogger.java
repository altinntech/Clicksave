package com.altinntech.clicksave.log;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * The {@code CSLogger} class provides logging functionality for the application.
 * It uses SLF4J as the logging framework.
 *
 * @author Fyodor Plotnikov
 */
@Slf4j
public class CSLogger {

    /**
     * Logs an informational message.
     *
     * @param message the message to log
     */
    public static void info(String message) {
        log.info(message);
    }

    public static void info(String context, String message) {
        log.info("<{}> {}", context, message);
    }

    /**
     * Logs a warning message.
     *
     * @param message the message to log
     */
    public static void warn(String message) {
        log.warn(message);
    }

    public static void warn(String context, String message) {
        log.warn("<" + context + "> " + message);
    }

    /**
     * Logs a debug message.
     *
     * @param message the message to log
     */
    public static void debug(String message) {
        if (log.isDebugEnabled()) {
            log.debug(message);
        }
    }

    public static void debug(String context, String message) {
        debug("<" + context + "> " + message);
    }

    /**
     * Logs an error message.
     *
     * @param message the message to log
     */
    public static void error(String message) {
        log.error(message);
    }

    /**
     * Logs an error message.
     *
     * @param message the message to log
     */
    public static void error(String message, Throwable cause) {
        log.error(message, cause);
    }

    /**
     * Logs an error message.
     *
     * @param message the message to log
     * @param source the source of log message
     */
    public static void error(String message, Class<?> source) {
        log.error("[{}] {}", source.getSimpleName(), message);
    }

    /**
     * Logs an important message.
     *
     * @param message the message to log
     */
    public static void important(String message) {
        log.info(MarkerFactory.getMarker("IMPORTANT"), message);
    }
}
