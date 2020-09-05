package com.domenico.server;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Persistence {

    private final static String pathname = "serverdata.json";
    private final static File dataFile = new File(pathname);

    /**
     * Rende i dati sul disco persistenti. La scrittura reale è bufferizzata quindi verrà svolta quando il buffer è pieno
     * altrimenti in fase di chiusura del programma.
     */
    public static void saveOnDisk(Map serverData) {
        //Scrivo sul file in maniera efficiente perchè prima bufferizzo il tutto e poi scrivo effettivamente
        //grandi blocchi di bytes
        try (Writer writer = new BufferedWriter(new FileWriter(dataFile, false))) {
            JSONValue.writeJSONString(serverData, writer);
        } catch (Exception ignored) {}
    }

    public static Map<String, UserData> readFromDisk() throws IOException {
        Map<String, UserData> data = new HashMap<>();
        //Leggo dal file se esiste già oppure se la creazione avviene con successo
        dataFile.createNewFile();
        try (Reader reader = new BufferedReader(new FileReader(dataFile))) {
            //Leggo dal file
            JSONObject obj = (JSONObject) JSONValue.parse(reader);
            if (obj != null) {  //Se il file contiene qualcosa
                for (Object key : obj.keySet()) {
                    JSONObject dataRead = (JSONObject) obj.get(key);
                    UserData userData = UserData.newFromJSON(dataRead);
                    data.put((String) key, userData);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }
}
