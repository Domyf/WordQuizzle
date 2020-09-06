package com.domenico.server;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MainClassWQServer {

    public static void main(String[] args) {
        try {
            List<String> italianWords = loadItalianWords();
            System.out.println("Loaded "+ italianWords.size() +" italian words");
            UserRegistrationService.newRegistrationService();
            WQServer server = new WQServer(italianWords);
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Loads from the local file all the italian words */
    private static List<String> loadItalianWords() throws IOException {
        ArrayList<String> words = new ArrayList<>();
        //file downloaded from https://github.com/napolux/paroleitaliane/tree/master/paroleitaliane
        Path path = Path.of("src/com/domenico/server/" + Settings.ITALIAN_WORDS_FILENAME);  //TODO update this
        BufferedReader reader = Files.newBufferedReader(path);
        String line;
        while ((line = reader.readLine()) != null) {
            words.add(line);
        }
        reader.close();
        return words;
    }

}
