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

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Not really unit tests that assert the randomness, per se. Rather, a place
 * to exercise the code and understand the implementation.
 * <p>
 * However, it might be worth implementing a MockRandomable where we can
 * control the output and assert with certainty the results of different
 * method calls.
 *
 * @see RandomNumber
 */
public class RandomNumberTest extends AbstractTest {

    private static final int COUNT = 40;
    private List<Integer> nums;

    @Test
    public void replacesMain() {
        title("replacesMain");

        RandomNumber.setRandomNumberGenerator(RandomNumberType.R250_521);
        int[] count = new int[6];
        int total = 100000;
        for (int i = 0; i < total; i++) {
            int d = RandomNumber.getDieRoll();
            count[d - 1]++;
        }
        for (int i = 0; i < 6; i++) {
            print("%d = %d which is %.03f%%", i + 1, count[i],
                    (((double) count[i] / (double) total) * 100D));
        }
    }

    @Test
    public void someFives() {
        title("someFives");
        nums = new ArrayList<>(COUNT);
        for (int i=0; i<COUNT; i++) {
            nums.add(RandomNumber.getRandom(5));
        }
        print(nums);
    }

    @Test
    public void d6() {
        title("d6");
        nums = new ArrayList<>(COUNT);
        for (int i=0; i<COUNT; i++) {
            nums.add(RandomNumber.getDieRoll());
        }
        print(nums);
    }

    @Test
    public void d20() {
        title("d20");
        nums = new ArrayList<>(COUNT);
        for (int i=0; i<COUNT; i++) {
            nums.add(RandomNumber.getDieRoll(20));
        }
        print(nums);
    }

    @Test
    public void highLow() {
        title("highLow (7..12)");
        nums = new ArrayList<>(COUNT);
        for (int i=0; i<COUNT; i++) {
            nums.add(RandomNumber.getHighLow(7, 12));
        }
        print(nums);
    }

    @Test
    public void percent70() {
        title("percent70");
        int count = 0;
        for (int i=0; i<1000; i++) {
            if (RandomNumber.percentRollSuccess(70)) {
                count++;
            }
        }
        print("Of 1000 rolls, %d were true", count);
    }

    @Test
    public void dieStringD2() {
        title("dieStringD2");
        int[] counts = new int[13]; // let's waste a couple of slots
        for (int i=0; i<1000; i++) {
            int result = RandomNumber.getFromDieString("D2");
            counts[result]++;
        }
        print(Arrays.toString(counts));
    }

    @Test
    public void dieStringD1plus3() {
        title("dieStringD1plus3");
        int[] counts = new int[10]; // let's waste a couple of slots
        for (int i=0; i<1000; i++) {
            int result = RandomNumber.getFromDieString("D1+3");
            counts[result]++;
        }
        print(Arrays.toString(counts));
    }
}
