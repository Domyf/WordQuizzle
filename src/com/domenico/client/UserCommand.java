package com.domenico.client;

import java.util.Arrays;

/** This class represents a command written by the user with its cmd and its parameters. Basically a command is in the
 * form <CMD> <Parameters>. For example aggiungi_amico usernameAmico. Ogni parametro ha il suo specifico gruppo
 * di parametri, i quali devono essere scritti in un determinato ordine. */
public class UserCommand {

    //List of all the commands that the user can type. It is called cmd
    public static final String REGISTER_USER = "registra_utente";
    public static final String LOGIN = "login";
    public static final String LOGOUT = "logout";
    public static final String ADD_FRIEND = "aggiungi_amico";
    public static final String FRIEND_LIST = "lista_amici";
    public static final String CHALLENGE = "sfida";
    public static final String SHOW_SCORE = "mostra_punteggio";
    public static final String SHOW_LEADERBOARD = "mostra_classifica";
    public static final String EXIT = "esci";
    public static final String HELP = "aiuto";

    private String cmd;
    private String[] params;

    /** Constructor that builds the command from the line typed by the user */
    public UserCommand(String line) {
        String[] splittedLine = line.split(" ");
        if (splittedLine.length > 0)
            cmd = splittedLine[0];
        if (splittedLine.length > 1)
            params = Arrays.copyOfRange(splittedLine, 1, splittedLine.length);
    }

    /** Getter method for the cmd string */
    public String getCmd() {
        return cmd;
    }

    /** Returns the param defined by the given index. It can throw IndexOutOfBoundException if the index is not valid */
    public String getParam(int index) {
        return params[index];
    }

    /** Returns true if this command has as much params as the integer passed by argument, false otherwise */
    public boolean hasParams(int howMany) {
        return params != null && params.length == howMany;
    }

    /** Returns the usage of the specified command */
    public static String getCommandUsage(String command) {
        switch (command) {
            case REGISTER_USER:
                return REGISTER_USER+" <nickUtente> <password> registra l' utente";
            case LOGIN:
                return LOGIN+" <nickUtente> <password> effettua il login";
            case LOGOUT:
                return LOGOUT+" effettua il logout";
            case ADD_FRIEND:
                return ADD_FRIEND+" <nickAmico> crea relazione di amicizia con nickAmico";
            case FRIEND_LIST:
                return FRIEND_LIST+" mostra la lista dei propri amici";
            case CHALLENGE:
                return CHALLENGE+" <nickAmico> richiesta di una sfida a nickAmico";
            case SHOW_SCORE:
                return SHOW_SCORE+" mostra il punteggio dell’utente";
            case SHOW_LEADERBOARD:
                return SHOW_LEADERBOARD+" mostra una classifica degli amici dell’utente (incluso l’utente stesso)";
            case HELP:
                return HELP+" mostra la lista dei comandi";
            case EXIT:
                return EXIT+" esce dal programma";
        }
        return "";
    }
}
