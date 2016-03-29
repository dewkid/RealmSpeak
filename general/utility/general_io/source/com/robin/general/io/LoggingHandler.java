/*
 * RealmSpeak is the Java application for playing the board game Magic Realm.
 * Copyright (c) 2005-2016 Robin Warren
 * E-mail: robin@dewkid.com
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see
 *
 * http://www.gnu.org/licenses/
 */
package com.robin.general.io;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.logging.ErrorManager;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Sends logging messages to the console, either System.out or System.err,
 * depending on the level of the LogRecord.
 */
public class LoggingHandler extends Handler {

    // at this level and above, output sent to System.err; below sent to System.out
    private static int ERR_LEVEL_VALUE_THRESHOLD = Level.WARNING.intValue();

    // has formatter header been printed
    private boolean doneHeader = false;

    @Override
    public void publish(LogRecord record) {
        if (!isLoggable(record)) {
            return;
        }
        String msg;
        try {
            msg = getFormatter().format(record);
        } catch (Exception ex) {
            // We don't want to throw an exception here, but we
            // report the exception to any registered ErrorManager.
            reportError(null, ex, ErrorManager.FORMAT_FAILURE);
            return;
        }
        try {
            PrintStream stream = null;
            if (record.getLevel().intValue() >= ERR_LEVEL_VALUE_THRESHOLD) {
                stream = System.err;
            } else {
                stream = System.out;
            }
            if (!doneHeader) {
                stream.print(getFormatter().getHead(this));
                doneHeader = true;
            }
            stream.print(msg);
        } catch (Exception ex) {
            // We don't want to throw an exception here, but we
            // report the exception to any registered ErrorManager.
            reportError(null, ex, ErrorManager.WRITE_FAILURE);
        }
    }

    @Override
    public void flush() {
        // not needed
    }

    @Override
    public void close() throws SecurityException {
        // if the formatter head has been printed, print the tail, but don't
        // need to close System.out or System.err
        // not sure whether to print tail to out or err.  Just do out for now.
        if (doneHeader) {
            try {
                System.out.print(getFormatter().getTail(this));
            } catch (Exception ex) {
                // We don't want to throw an exception here, but we
                // report the exception to any registered ErrorManager.
                reportError("Failure writing tail", ex, ErrorManager.WRITE_FAILURE);
            }
        }
    }

    /**
     * Initialize the logging subsystem.
     * <p>
     * Logging levels for specific loggers can be set through environment
     * variables. For example:
     * <pre>
     * -Dcom.robin.magic_realm.RealmSpeak.RealmGameHandler=FINE
     * </pre>
     */
    public static void initLogging() {
        LoggingHandler loggingHandler = new LoggingHandler();
        loggingHandler.setLevel(Level.ALL);
        loggingHandler.setFormatter(new LoggingFormatter());
        Logger rootLogger = Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        for (int i = 0; i < handlers.length; i++) {
            rootLogger.removeHandler(handlers[0]);
        }
        rootLogger.addHandler(loggingHandler);
        rootLogger.setLevel(Level.WARNING); // default to warning

        for (Iterator i = System.getProperties().keySet().iterator(); i.hasNext(); ) {
            String propertyKey = (String) i.next();
            if (propertyKey.startsWith("com")) {
                Logger aLogger = Logger.getLogger(propertyKey);
                String property = System.getProperty(propertyKey);
                aLogger.setLevel(Level.parse(property));
                System.out.println("Logging for " + propertyKey + ": " + property);
            }
        }
    }
}