package com.robin.general.graphics;

/**
 * Abstract base class for unit tests.
 */
public abstract class AbstractTest {

    /**
     * Tolerance for double comparisons.
     */
    protected static final double TOLERANCE = 1e-9;

    /**
     * Prints formatted output.
     *
     * @see String#format(String, Object...)
     * @param fmt format string
     * @param items positional items
     */
    protected void print(String fmt, Object... items) {
        System.out.println(String.format(fmt, items));
    }

    /**
     * Prints the string value of the given object.
     *
     * @param object the object to print
     */
    protected void print(Object object) {
        print("%s", object);
    }
}
