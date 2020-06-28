package com.domenico.shared;

import java.util.Iterator;

/**
 * Class that contains some useful methods for general purposes
 */
public class Utils {

    /**
     * Given a iterable object, this method generates a string that contains all the elements of the iterable object.
     * The elements are separated by the given divider. The divider is not present at the end of the string. The returned
     * string doesn't contain the new line character at the end.
     * @param iterable an iterable object
     * @param divider the string that is used to separate each element
     * @return a string that contains all the elements of the iterable. Each element is separated by the given divider
     */
    public static String stringify(Iterable<Object> iterable, String divider) {
        StringBuilder builder = new StringBuilder();
        Iterator<Object> iterator = iterable.iterator();
        while (iterator.hasNext()) {
            Object next = iterator.next();
            builder.append(next);
            if (iterator.hasNext())
                builder.append(divider);
        }

        return builder.toString();
    }

    /**
     * Given a an array, this method generates a string that contains all the elements of the array. The elements are
     * separated by the given divider. The divider is not present at the end of the string. The returned string
     * doesn't contain the new line character at the end.
     * @param objects an array of objects
     * @param divider the string that is used to separate each element
     * @return a string that contains all the elements of the array. Each element is separated by the given divider
     */
    public static String stringify(Object[] objects, String divider) {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (; i < objects.length-1; i++) {
            builder.append(objects[i]).append(divider);
        }
        if (i < objects.length)
            builder.append(objects[i]);
        return builder.toString();
    }
}
