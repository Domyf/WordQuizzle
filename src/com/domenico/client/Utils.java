package com.domenico.client;

import java.util.Iterator;

public class Utils {

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
}
