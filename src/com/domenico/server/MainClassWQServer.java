package com.domenico.server;

import com.domenico.server.network.UserRegistrationService;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/** Starting point for the WordQuizzle server. It loads the settings and all the italian words from the resources.
 * It also start the RMI service and the WordQuizzle server. */
public class MainClassWQServer {

    public static final String SETTINGS_FILE = "wordquizzle.properties";

    public static void main(String[] args) {
        try {
            //Load settings
            InputStream settingsStream = getFileFromResources(SETTINGS_FILE);
            Settings.loadSettings(settingsStream);
            System.out.println("Settings loaded from "+SETTINGS_FILE+" file");
            //Load italian words
            InputStream inputStream = getFileFromResources(Settings.getItalianWordsFilename());
            List<String> italianWords = loadItalianWords(inputStream);
            System.out.println("Loaded "+ italianWords.size() +" italian words");
            //Run registration service via RMI
            UserRegistrationService.newRegistrationService();
            //Run server
            WQServer server = new WQServer(italianWords);
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Loads from the local file all the italian words.
     *  File downloaded from https://github.com/napolux/paroleitaliane/tree/master/paroleitaliane
     * */
    private static List<String> loadItalianWords(InputStream inputStream) throws IOException {
        ArrayList<String> words = new ArrayList<>();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            words.add(line);
        }
        bufferedReader.close();
        return words;
    }

    /** Returns the input stream related to the file specified by its name which is inside the server's resources folder */
    private static InputStream getFileFromResources(String filename) {
        ClassLoader classLoader = MainClassWQServer.class.getClassLoader();
        return classLoader.getResourceAsStream(filename);
    }
}
