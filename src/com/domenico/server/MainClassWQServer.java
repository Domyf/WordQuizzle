package com.domenico.server;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MainClassWQServer {

    private static List<String> italianWords;

    public static void main(String[] args) {
        try {
            italianWords = loadItalianWords(Constants.ITALIAN_WORDS_FILENAME);
            System.out.println("Loaded "+italianWords.size()+" italian words");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<String> loadItalianWords(String filename) throws IOException {
        ArrayList<String> words = new ArrayList<>();
        //words from https://github.com/napolux/paroleitaliane/tree/master/paroleitaliane
        BufferedReader reader = Files.newBufferedReader(Path.of("src/com/domenico/server/"+filename));
        String line = null;
        while ((line = reader.readLine()) != null) {
            words.add(line);
        }

        return words;
    }
}
