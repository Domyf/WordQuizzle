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
        /*TODO rimuovere le frasi seguenti
        The point of BufferedWriter is basically to consolidate lots of little writes into far fewer big writes,
        as that's usually more efficient (but more of a pain to code for). You shouldn't need to do anything special
        to get it to work properly though, other than making sure you flush it when you're finished with it
        - and calling close() will do this and flush/close the underlying writer anyway.
         */
        try (Writer writer = new BufferedWriter(new FileWriter(dataFile, false))) {
            //Scrivo sul file in maniera efficiente perchè prima bufferizzo il tutto e poi scrivo effettivamente
            //grandi blocchi di bytes
            JSONValue.writeJSONString(serverData, writer);
        } catch (Exception e) {}
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
