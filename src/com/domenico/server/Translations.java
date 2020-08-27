package com.domenico.server;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
//TODO fare doc
public class Translations {

    private final static int START_LEN = 34;
    private final static int BUF_LEN = 16;
    private final static String WORD_DIVIDER = ";";


    public static String[] translate(String[] itWords) {
        if (itWords != null && itWords.length == 0)
            return new String[0];
        StringBuilder words = new StringBuilder();
        for (String word :itWords) {
            words.append(word).append(WORD_DIVIDER);
        }
        String result = "";
        try {
            URL url = new URL("https://api.mymemory.translated.net/get?q="+words+"&langpair=it|en");
            result = getTranslationsFromURL(url);
        } catch (IOException ignored) { }

        return result.split(WORD_DIVIDER);
    }

    private static String getTranslationsFromURL(URL url) throws IOException {
        InputStream is = url.openStream();
        //skip the starting bytes
        is.skipNBytes(START_LEN);
        StringBuilder translations = new StringBuilder();
        int end_index = -1;
        byte[] bytes = new byte[0];
        //Find the end index
        while (end_index == -1) {
            end_index = 0;
            bytes = is.readNBytes(BUF_LEN);
            if (bytes.length == 0) { //end of file, something went wrong
                end_index = -1;
                break;
            }
            translations.append(new String(bytes, StandardCharsets.UTF_8));
            end_index = translations.indexOf("\"", end_index + 1);
        }
        //Close the stream
        is.close();
        //Get the words
        if (end_index != -1)
            return translations.substring(1, end_index);
        return "";
    }
}
