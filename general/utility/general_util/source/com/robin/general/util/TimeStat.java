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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

public class TimeStat {

//    private static final long NANOS_PER_MILLISECOND = 1000000l;

    /**
     * Hash that holds a single start time for any single key.
     */
    private Hashtable starts = new Hashtable();

    /**
     * Hash that holds all the measurement (duration) times for any single key.
     */
    private Hashtable times = new Hashtable();

    /**
     * Sole constructor.
     */
    public TimeStat() {
        reset();
    }

    /**
     * Resets all measurements by deleting all times.
     */
    public void reset() {
        starts = new Hashtable();
        times = new Hashtable();
    }

    /**
     * Marks the start time for a given measurement, identified by the key.
     * Any previous start time is overwritten.
     * 
     * @param key the measurement key
     */
    public void markStartTime(String key) {
        starts.put(key, new Timestamp((new java.util.Date()).getTime()));
    }

    /**
     * Marks the end time for a given measurement, identified by the key.
     * This method enters a new measurement and deletes the reference from
     * the start time hash. If there is no corresponding start time,
     * nothing happens.
     *
     * @param key the measurement key
     */
    public void markEndTime(String key) {
        Timestamp end = new Timestamp((new java.util.Date()).getTime());
        Timestamp start = (Timestamp) starts.get(key);
        if (start != null) {
            starts.remove(key);
            long endMs = end.getTime();
                // + (long)end.getNanos()/NANOS_PER_MILLISECOND;
            long startMs = start.getTime();
                // + (long)start.getNanos()/NANOS_PER_MILLISECOND;
            long diff = endMs - startMs;
            ArrayList all = (ArrayList) times.get(key);
            if (all == null) {
                all = new ArrayList();
                times.put(key, all);
            }
            all.add(new Long(diff));
        }
    }

    /**
     * Returns a multi-line summary of all measurement keys and their
     * timed averages.
     *
     * @return multi-line summary
     */
    public String getAverageSummary() {
        StringBuffer sb = new StringBuffer("Average Summary:\n\n");
        for (Enumeration e = times.keys(); e.hasMoreElements(); ) {
            String key = (String) e.nextElement();
            double avgmSec = getAverageMilliseconds(key);
            sb.append("     " + key + " averaged " + avgmSec +
                    " milliseconds. (" + getTotalMeasurements(key) +
                    " total measurements)\n");
        }
        sb.append("\n");
        return sb.toString();
    }

    /**
     * Returns an enumeration of all measurement keys.
     *
     * @return measurement key enumeration
     */
    public Enumeration keys() {
        return times.keys();
    }


    /**
     * Returns the total number of measurements for the given key.
     *
     * @param key the measurement key
     * @return the number of measurements
     */
    public int getTotalMeasurements(String key) {
        ArrayList all = (ArrayList) times.get(key);
        if (all != null) {
            return all.size();
        }
        return 0;
    }

    /**
     * Returns the average number of milliseconds duration for each
     * measurement for the specified key.
     *
     * @param key the measurement key
     * @return average measurement duration
     */
    public double getAverageMilliseconds(String key) {
        ArrayList all = (ArrayList) times.get(key);
        if (all != null) {
            long total = 0;
            for (Iterator i = all.iterator(); i.hasNext(); ) {
                Long msec = (Long) i.next();
                total += msec.longValue();
            }
            return ((double) total / (double) all.size());
        }
        return 0.0;
    }

    /**
     * Returns the total number of milliseconds of each start/end duration
     * for the measurement with the given key.
     *
     * @param key the measurement key
     * @return total measurement duration
     */
    public double getTotalMilliseconds(String key) {
        ArrayList all = (ArrayList) times.get(key);
        if (all != null) {
            long total = 0;
            for (Iterator i = all.iterator(); i.hasNext(); ) {
                Long msec = (Long) i.next();
                total += msec.longValue();
            }
            return (double) total;
        }
        return 0.0;
    }
}