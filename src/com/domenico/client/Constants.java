package com.domenico.client;

/** This is just a class with the only job to define all the commands as constants */
public class Constants {
    
    public static final String REGISTER_USER = "registra_utente";
    public static final String LOGIN = "login";
    public static final String LOGOUT = "logout";
    public static final String ADD_FRIEND = "aggiungi_amico";
    public static final String FRIEND_LIST = "lista_amici";
    public static final String CHALLENGE = "sfida";
    public static final String SHOW_SCORE = "mostra_punteggio";
    public static final String SHOW_LEADERBOARD = "mostra_classifica";
    public static final String EXIT = "esci";

    //The usage of this program
    public static final String USAGE = "usage : COMMAND [ ARGS ...]\n" +
            "Commands:\n" +
            "   "+REGISTER_USER+" <nickUtente> <password> registra l' utente\n"+
            "   "+LOGIN+" <nickUtente> <password> effettua il login\n" +
            "   "+LOGOUT+" effettua il logout\n" +
            "   "+ADD_FRIEND+" <nickAmico> crea relazione di amicizia con nickAmico\n" +
            "   "+FRIEND_LIST+" mostra la lista dei propri amici\n" +
            "   "+CHALLENGE+" <nickAmico> richiesta di una sfida a nickAmico\n" +
            "   "+SHOW_SCORE+" mostra il punteggio dell’utente\n" +
            "   "+SHOW_LEADERBOARD+" mostra una classifica degli amici dell’utente (incluso l’utente stesso)\n" +
            "   "+EXIT+" esce dal programma";
}
