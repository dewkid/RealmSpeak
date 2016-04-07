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

import java.util.Random;
import java.util.StringTokenizer;

/**
 * Provides pseudo-random number generation functionality.
 */
public class RandomNumber {

    private Randomable rg;
    private long seed;
    private long count;

    private RandomNumber() {
        this(System.nanoTime(), 0);
    }

    private RandomNumber(long seed, long count) {
        this.seed = seed;
        this.count = count;
        rg = getRandomable(seed);
        for (int i = 0; i < count; i++) {
            rg.nextInt();
        }
    }

    private int rand(int val) {
        if (val <= 0)
            return 0;
        count++;
        return rg.nextInt(val);
    }

    private Randomable getRandomable(long seed) {
        switch (currentRandomNumberType) {
            case R250_521:
                return new R250_521(seed);
            case MersenneTwister:
                return new MersenneTwister(seed);
        }
        return new JavaRandom(seed);
    }

    // STATIC METHODS

    private static RandomNumberType currentRandomNumberType = RandomNumberType.System;

    /**
     * Selects the random number generator implementation to use.
     *
     * @param rt the generator type to use
     */
    public static void setRandomNumberGenerator(RandomNumberType rt) {
        if (currentRandomNumberType != rt) {
            currentRandomNumberType = rt;
            soleInstance = null;
        }
    }

    /**
     * Returns the currently set random number generator type.
     *
     * @return the generator type
     */
    public static RandomNumberType getRandomNumberGenerator() {
        return currentRandomNumberType;
    }

    private static RandomNumber soleInstance = null;

    private static RandomNumber getSoleInstance() {
        if (soleInstance == null) {
            soleInstance = new RandomNumber();
        }
        return soleInstance;
    }

    /**
     * Returns true if the generator instance has been initialized.
     *
     * @return true, if initialized
     */
    public static boolean hasBeenInitialized() {
        return soleInstance != null;
    }

    /**
     * Returns a random integer from the range 0 .. val-1.
     *
     * @param val the upper bound
     * @return a random number from this range
     */
    public static int getRandom(int val) {
        return getSoleInstance().rand(val);
    }

    /**
     * Returns the result of rolling a six-sided die.
     * That is, a random number from the range 1..6 (inclusive).
     *
     * @return a randomly chosen die-roll
     */
    public static int getDieRoll() {
        return getDieRoll(6);
    }

    /**
     * Returns the result of rolling a die with the given number of sides.
     * That is, a random number from the range 1..dieSides (inclusive).
     *
     * @param dieSides number of sides
     * @return a randomly chosen result
     */
    public static int getDieRoll(int dieSides) {
        return getRandom(dieSides) + 1;
    }

    /**
     * Returns a random integer from the range low .. high. This works with
     * negative numbers too.
     *
     * @param low  the lower bound (inclusive)
     * @param high the upper bound (inclusive)
     * @return a random integer from the given range
     */
    public static int getHighLow(int low, int high) {
        int range = high - low + 1;
        return getRandom(range) + low;
    }

    /**
     * Emulates rolling a 100-sided die and returns true if the value rolled
     * is less than or equal to the specified percent. This method will always
     * return true if percent is 100.
     *
     * @param percent the percent to test for
     * @return true, if percent successful
     */
    public static boolean percentRollSuccess(int percent) {
        return getRandom(100) < percent;
    }

    /**
     * Returns the result of a dice roll with modifier; for example:
     * "1D+2" or "3D-1".
     *
     * @param count dice count with modifier
     * @return the result
     */
    public static int getFromDieString(String count) {
        int ret;
        count = count.toUpperCase();
        if (count.indexOf('D') >= 0) {
            StringTokenizer st = new StringTokenizer(count, "D+");
            int dice = Integer.valueOf(st.nextToken());
            int mod = 0;
            if (st.hasMoreTokens()) {
                mod = Integer.valueOf(st.nextToken());
            }
            int total = 0;
            for (int i = 0; i < dice; i++) {
                total += RandomNumber.getDieRoll();
            }
            total += mod;
            ret = total;
        } else {
            ret = Integer.valueOf(count).intValue();
        }
        return ret;
    }


    /**
     * Returns the initial seed for the generator.
     *
     * @return the initial seed
     */
    public static long getSeed() {
        return getSoleInstance().seed;
    }

    /**
     * Returns the count of random numbers generated.
     *
     * @return count of numbers generated
     */
    public static long getCount() {
        return getSoleInstance().count;
    }

    /**
     * Initialize the generator with the given initial seed and count.
     *
     * @param seed  initial seed
     * @param count initial count
     */
    public static void init(long seed, long count) {
        soleInstance = new RandomNumber(seed, count);
        //(new Exception()).printStackTrace();
        // UNCOMMENT THIS LINE WHEN THINGS DONT SEEM RANDOM ANYMORE...
        // THEN YOU'LL SEE WTF!
    }


    /**
     * An object that provides random integers.
     */
    private interface Randomable {

        /**
         * Returns a random number from the range 0 to {@code mod - 1}.
         *
         * @param mod the bound on the range of numbers to consider.
         * @return a random integer from the specified range
         */
        int nextInt(int mod);

        /**
         * Returns a random integer.
         *
         * @return a random integer
         */
        int nextInt();
    }

    /**
     * Java library based Randomable.
     */
    private final class JavaRandom implements Randomable {
        private Random random;

        public JavaRandom(long seed) {
            random = new Random(seed);
        }

        @Override
        public int nextInt(int mod) {
            return random.nextInt(mod);
        }

        @Override
        public int nextInt() {
            return random.nextInt();
        }
    }


    /**
     * R250/521 based Randomable.
     * <p>
     * See http://www.qbrundage.com/michaelb/pubs/essays/random_number_generation.html
     */
    private final class R250_521 implements Randomable {
        private int r250_index;
        private int r521_index;
        private int[] r250_buffer = new int[250];
        private int[] r521_buffer = new int[521];

        /**
         * Constructs an R250/521 random number generator.
         *
         * @param seed initial seed
         */
        public R250_521(long seed) {
            Random r = new Random(seed);
            int i = 521;
            int mask1 = 1;
            int mask2 = 0xFFFFFFFF;

            while (i-- > 250) {
                r521_buffer[i] = r.nextInt();
            }
            while (i-- > 31) {
                r250_buffer[i] = r.nextInt();
                r521_buffer[i] = r.nextInt();
            }

            /*
             * Establish linear independence of the bit columns by setting the
             * diagonal bits and clearing all bits above
             */
            while (i-- > 0) {
                r250_buffer[i] = (r.nextInt() | mask1) & mask2;
                r521_buffer[i] = (r.nextInt() | mask1) & mask2;
                mask2 ^= mask1;
                mask1 >>= 1;
            }
            r250_buffer[0] = mask1;
            r521_buffer[0] = mask2;
            r250_index = 0;
            r521_index = 0;
        }

        @Override
        public int nextInt(int mod) {
            return Math.abs(nextInt()) % mod;
        }

        @Override
        public int nextInt() {
            int i1 = r250_index;
            int i2 = r521_index;

            int j1 = i1 - (250 - 103);
            if (j1 < 0)
                j1 = i1 + 103;
            int j2 = i2 - (521 - 168);
            if (j2 < 0)
                j2 = i2 + 168;

            int r = r250_buffer[j1] ^ r250_buffer[i1 % 250];
            r250_buffer[i1 % 250] = r;
            int s = r521_buffer[j2] ^ r521_buffer[i2 % 521];
            r521_buffer[i2 % 521] = s;

            i1 = (i1 != 249) ? (i1 + 1) : 0;
            r250_index = i1;
            i2 = (i2 != 521) ? (i2 + 1) : 0;
            r521_index = i2;

            return r ^ s;
        }
    }

    /**
     * Mersenne Twister based Randomable.
     */
    private final class MersenneTwister implements Randomable {
        private int mt_index;
        private int[] mt_buffer = new int[624];

        /**
         * Constructs a Mersenne Twister random number generator.
         *
         * @param seed initial seed
         */
        public MersenneTwister(long seed) {
            Random r = new Random(seed);
            for (int i = 0; i < 624; i++)
                mt_buffer[i] = r.nextInt();
            mt_index = 0;
        }

        @Override
        public int nextInt(int mod) {
            return Math.abs(nextInt()) % mod;
        }

        @Override
        public int nextInt() {
            if (mt_index == 624) {
                mt_index = 0;
                int i = 0;
                int s;
                for (; i < 624 - 397; i++) {
                    s = (mt_buffer[i] & 0x80000000) | (mt_buffer[i + 1] & 0x7FFFFFFF);
                    mt_buffer[i] = mt_buffer[i + 397] ^ (s >> 1) ^ ((s & 1) * 0x9908B0DF);
                }
                for (; i < 623; i++) {
                    s = (mt_buffer[i] & 0x80000000) | (mt_buffer[i + 1] & 0x7FFFFFFF);
                    mt_buffer[i] = mt_buffer[i - (624 - 397)] ^ (s >> 1) ^ ((s & 1) * 0x9908B0DF);
                }

                s = (mt_buffer[623] & 0x80000000) | (mt_buffer[0] & 0x7FFFFFFF);
                mt_buffer[623] = mt_buffer[396] ^ (s >> 1) ^ ((s & 1) * 0x9908B0DF);
            }
            return mt_buffer[mt_index++];
        }
    }
}