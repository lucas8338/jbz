package jbz.frontEnd.logger;

import org.apache.commons.logging.impl.SimpleLog;

import java.io.File;

public class Logger {
    /**
     * the location of the logger file, if there any.
     */
    public final File path = null;

    public final SimpleLog logger;

    public Logger(String name, Logger_level level) {
        this.logger = new SimpleLog(name);
        this.setLevel(level);
    }

    public void setLevel(Logger_level level) {
        if (level.equals(Logger_level.info)) {
            this.logger.setLevel(SimpleLog.LOG_LEVEL_INFO);
        } else if (level.equals(Logger_level.debug)) {
            this.logger.setLevel(SimpleLog.LOG_LEVEL_ALL);
        }
    }

    public void info(String message) {
        this.logger.info(message);
    }

    public void debug(String message) {
        this.logger.debug(message);
    }
}
