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
package com.robin.general.util;

/**
 * Simple stopwatch for timing code execution.
 */
public class StopWatch {

    private long startTime = 0;
    private long stopTime = 0;
    private boolean running = false;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(startTime);
        sb.append(" to ");
        sb.append(stopTime);
        return sb.toString();
    }

    /**
     * Starts the watch.
     */
    public void start() {
        this.startTime = System.nanoTime();
        this.running = true;
    }

    /**
     * Stops the watch.
     */
    public void stop() {
        this.stopTime = System.nanoTime();
        this.running = false;
    }

    /**
     * Returns the elapsed time in nanoseconds.
     *
     * @return elapsed time in nanoseconds
     */
    public long getElapsedTime() {
        long elapsed;
        if (running) {
            elapsed = (System.nanoTime() - startTime);
        } else {
            elapsed = (stopTime - startTime);
        }
        return elapsed;
    }


    /**
     * Returns the elapsed time in milliseconds.
     *
     * @return elapsed time in milliseconds
     */
    public long getElapsedTimeMilliSecs() {
        long elapsed;
        if (running) {
            elapsed = ((System.currentTimeMillis() - startTime) / 1000);
        } else {
            elapsed = ((stopTime - startTime) / 1000);
        }
        return elapsed;
    }

    /**
     * Sample usage.
     *
     * @param args (ignored)
     */
    public static void main(String[] args) {
        StopWatch s = new StopWatch();
        s.start();
        //code you want to time goes here
        s.stop();
        System.out.println("elapsed time in nanoseconds: " + s.getElapsedTime());
    }
}