package com.altinntech.clicksave.log;

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
public class CSLogger {
    private static final Logger logger = LoggerFactory.getLogger(CSLogger.class);
    private static final Marker IMPORTANT = MarkerFactory.getMarker("IMPORTANT");

    /**
     * Logs an informational message.
     *
     * @param message the message to log
     */
    public static void info(String message) {
        logger.info(message);
    }

    public static void info(String context, String message) {
        logger.info("<" + context + "> " + message);
    }

    /**
     * Logs a warning message.
     *
     * @param message the message to log
     */
    public static void warn(String message) {
        logger.warn(message);
    }

    public static void warn(String context, String message) {
        logger.warn("<" + context + "> " + message);
    }

    /**
     * Logs a debug message.
     *
     * @param message the message to log
     */
    public static void debug(String message) {
        if (logger.isDebugEnabled()) {
            logger.debug(message);
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
        logger.error(message);
    }

    /**
     * Logs an error message.
     *
     * @param message the message to log
     */
    public static void error(String message, Throwable cause) {
        logger.error(message, cause);
    }

    /**
     * Logs an error message.
     *
     * @param message the message to log
     * @param source the source of log message
     */
    public static void error(String message, Class<?> source) {
        logger.error("[" + source.getSimpleName() + "] " + message);
    }

    /**
     * Logs an important message.
     *
     * @param message the message to log
     */
    public static void important(String message) {
        logger.info(IMPORTANT, message);
    }
}
