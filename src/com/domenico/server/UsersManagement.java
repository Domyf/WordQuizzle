package com.domenico.server;

import org.json.simple.*;

import java.io.*;
import java.util.*;

//TODO write documentation for this class
public class UsersManagement {

    private static UsersManagement instance;
    private final static File dataFile = new File("serverdata.json");
    private HashMap<String, UserData> serverData; //map username -> user data

    private UsersManagement() throws IOException {
        this.serverData = readFromDisk();
    }

    public static UsersManagement getInstance() throws IOException {
        if (instance == null)
            instance = new UsersManagement();
        return instance;
    }

    public void register(String username, String password) throws UsersManagementException {
        if (serverData.get(username) != null)
            throw new UsersManagementException("Non è possibile registrarsi: questo nome utente è già utilizzato");

        serverData.put(username, new UserData(password));
        saveOnDisk();
    }

    public void login(String username, String password) throws UsersManagementException {
        UserData userData = serverData.get(username);
        if (userData == null || !userData.hasPassword(password))
            throw new UsersManagementException("Username o password non valida");
        if (userData.isOnline())
            throw new UsersManagementException("Hai già eseguito il login");

        userData.setOnline(true);
    }

    public void logout(String username) throws UsersManagementException {
        UserData userData = serverData.get(username);
        if (userData == null)
            throw new UsersManagementException("Username non valida");
        if (!userData.isOnline())
            throw new UsersManagementException("Hai già eseguito il logout");

        userData.setOnline(false);
    }

    public void addFriend(String username, String friendUsername) throws UsersManagementException {
        if (username.equals(friendUsername))
            throw new UsersManagementException("Non puoi diventare amico con te stesso");
        UserData first = serverData.get(username);
        UserData second = serverData.get(friendUsername);
        if (first == null)
            throw new UsersManagementException("Username non valida");
        if (second == null)
            throw new UsersManagementException("Non esiste un altro utente con questa username");

        if (areFriends(username, friendUsername)) {
            throw new UsersManagementException("L'amicizia non è stata creata perchè siete già amici");
        } else {
            first.addFriend(friendUsername);
            second.addFriend(username);
            saveOnDisk();
        }
    }

    public int getScore(String username) throws UsersManagementException {
        UserData userData = serverData.get(username);
        if (userData == null)
            throw new UsersManagementException("Username non valida");
        return userData.getScore();
    }

    public boolean isOnline(String username) {
        UserData userData = serverData.get(username);
        return userData != null && userData.isOnline();
    }

    public boolean areFriends(String firstUsername, String secondUsername) {
        UserData firstData = serverData.get(firstUsername);
        if (firstData != null)
            return firstData.hasFriend(secondUsername);
        return false;
    }

    public String getJSONFriendList(String username) throws UsersManagementException {
        UserData userData = serverData.get(username);
        if (userData == null)
            throw new UsersManagementException("Username non valida");
        return userData.getFriendList();
    }

    /**
     * Rende i dati sul disco persistenti. La scrittura reale è bufferizzata quindi verrà svolta quando il buffer è pieno
     * altrimenti in fase di chiusura del programma.
     */
    private void saveOnDisk() {
        /*TODO rimuovere le frasi seguenti
        The point of BufferedWriter is basically to consolidate lots of little writes into far fewer big writes,
        as that's usually more efficient (but more of a pain to code for). You shouldn't need to do anything special
        to get it to work properly though, other than making sure you flush it when you're finished with it
        - and calling close() will do this and flush/close the underlying writer anyway.
         */
        try (Writer writer = new BufferedWriter(new FileWriter(dataFile, false))) {
            //BufferedWriter bufferedWriter = Files.newBufferedWriter(filepath, StandardOpenOption.TRUNCATE_EXISTING);
            //Scrivo sul file in maniera efficiente perchè prima bufferizzo il tutto e poi scrivo effettivamente grandi blocchi di bytes
            JSONValue.writeJSONString(serverData, writer);
        } catch (Exception e) {}
    }

    private HashMap<String, UserData> readFromDisk() throws IOException {
        HashMap<String, UserData> data = new HashMap<>();
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
