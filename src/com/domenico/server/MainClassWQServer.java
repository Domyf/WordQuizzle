package com.domenico.server;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MainClassWQServer {

    public static final String USERS_FILENAME = "users.json";
    public static final String ITALIAN_WORDS_FILENAME = "1000_common_italian_words.txt";
    private static List<String> italianWords;

    public static void main(String[] args) {
        try {
            italianWords = loadItalianWords(ITALIAN_WORDS_FILENAME);
            System.out.println("Loaded "+ italianWords.size() +" italian words");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        try {
            UserRegistrationService.newRegistrationService();
            WQServer server = new WQServer();
            server.startProcessing();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<String> loadItalianWords(String filename) throws IOException {
        ArrayList<String> words = new ArrayList<>();
        //file downloaded from https://github.com/napolux/paroleitaliane/tree/master/paroleitaliane
        BufferedReader reader = Files.newBufferedReader(Path.of("src/com/domenico/server/"+filename));
        String line = null;
        while ((line = reader.readLine()) != null) {
            words.add(line);
        }
        reader.close();
        return words;
    }

}
