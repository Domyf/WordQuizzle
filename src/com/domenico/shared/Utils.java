package com.domenico.shared;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Class that contains some useful methods for general purposes
 */
public class Utils {

    /**
     * Given a iterable object, this method generates a string that contains all the elements of the iterable object.
     * The elements are separated by the given divider. The divider is not present at the end of the string. The returned
     * string doesn't contain the new line character at the end.
     * @param iterable an iterable object
     * @param divider the string that will separate each element
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
     * @param divider the string that will separate each element
     * @return a string that contains all the elements of the array. Each element is separated by the given divider
     */
    public static String stringify(Object[] objects, String divider) {
        return stringify(divider, objects);
    }

    /**
     * Same implementation of {@link #stringify(Object[], String)} but with an array of objects.
     * Look at {@link #stringify(Object[], String)} documentation for information about this method.
     */
    public static String stringify(String divider, Object ...objects) {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (; i < objects.length-1; i++) {
            builder.append(objects[i]).append(divider);
        }
        if (i < objects.length)
            builder.append(objects[i]);
        return builder.toString();
    }

    /**
     * Returns a new string in which the given string is centered and the length is as much as the parameter width.
     *
     * @param s the string to be centered
     * @param width the resulting length
     * @return the centered string
     */
    public static String getCenteredString(String s, int width) {
        int right = s.length() + (width - s.length()) / 2;    //How much space to leave on the right side
        return String.format("%-" + width  + "s", String.format("%" + right + "s", s));
    }

    /**
     * Creates a list with a desired size that contains random elements from the given list. Each element in the
     * created list are different.
     *
     * @param list the list from where the elements should be taken
     * @param desiredSize the desired size of the random sub list
     * @param dest where to add the entire sublist
     * @param <T> the type of each list's element
     */
    public static <T> void randomSubList(List<T> list, int desiredSize, List<T> dest) {
        if (desiredSize > list.size())
            throw new IllegalArgumentException("The list has less elements that the ones required");
        ThreadLocalRandom random = ThreadLocalRandom.current();

        int i = 0;
        while( i < desiredSize) {
            int index = random.nextInt(list.size());
            T elem = list.get(index);
            if (!dest.contains(elem)) {
                dest.add(elem);
                i++;
            }
        }
    }

    /**
     * Encrypts the given string by using the given algorithm
     * @param string the string that has to be encrypted
     * @param algorithm the algorithm that should be used
     * @return the encrypted string by using the given algorithm
     */
    public static String encrypt(String string, String algorithm) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        byte[] hash = md.digest(string.getBytes(StandardCharsets.UTF_8));
        //transforms bytes into hexadecimal string
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
