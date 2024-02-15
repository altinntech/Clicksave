package com.altinntech.clicksave.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class CSLogger {
    private static final Logger logger = LoggerFactory.getLogger(CSLogger.class);
    private static final Marker IMPORTANT = MarkerFactory.getMarker("IMPORTANT");

    public static void info(String message) {
        logger.info(message);
    }

    public static void debug(String message) {
        if (logger.isDebugEnabled()) {
            logger.debug(message);
        }
    }

    public static void error(String message) {
        logger.error(message);
    }

    public static void important(String message) {
        logger.info(IMPORTANT, message);
    }
}
