package com.domenico.server;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

//TODO fare doc
public class Translations {

    private final static int START_LEN = 34;
    private final static int BUF_LEN = 16;
    private final static String WORD_DIVIDER = ".%20";

    public static String[] translate(List<String> itWords) {
        if (itWords == null || itWords.isEmpty())
            return new String[0];

        StringBuilder words = new StringBuilder();
        int i = 0;
        for (; i < itWords.size()-1; i++) {
            words.append(itWords.get(i)).append(WORD_DIVIDER);
        }
        if (i < itWords.size()) {
            words.append(itWords.get(itWords.size() - 1));
        }

        String result = "";
        try {
            URL url = new URL("https://api.mymemory.translated.net/get?q="+words+"&langpair=it|en");
            result = getTranslationsFromURL(url);
        } catch (IOException ignored) { }

        return result.split(WORD_DIVIDER);
    }

    /**
     * Sends a HTTP GET request to the MyMemory web server, parses the response and returns it.
     *
     * @param url to what URL the HTTP GET request should be done
     * @return the translation gave by the MyMemory Rest API
     * @throws IOException an error occurs while reading the web server response
     */
    private static String getTranslationsFromURL(URL url) throws IOException {
        InputStream is = url.openStream();
        //skip the starting bytes
        is.skipNBytes(START_LEN);
        StringBuilder translations = new StringBuilder();
        int end_index = -1;
        byte[] bytes;
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
        System.out.println(end_index+" "+translations);
        }
        //Close the stream
        is.close();
        //Get the words
        if (end_index != -1)
            return translations.substring(1, end_index);
        return "";
    }
}
