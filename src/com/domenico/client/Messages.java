package com.domenico.client;

/** Stores all the messages showed to the client */
public class Messages {

    //Success
    public static final String LOGIN_SUCCESS = "Login eseguito con successo";
    public static final String LOGOUT_SUCCESS = "Logout eseguito con successo";

    //Warnings or errors
    public static final String LOGIN_NEEDED = "Per eseguire questa operazione hai bisogno di fare il login";
    public static final String LOGOUT_NEEDED = "Devi eseguire il logout prima di fare questa operazione";
    public static final String ASK_LOGIN = "Vuoi eseguire il login?";
    public static final String INVALID_COMMAND = "Questo comando non esiste. Ecco una lista dei comandi disponibili ed il loro utilizzo";
    public static final String BAD_COMMAND_SINTAX = "Comando non corretto. Sintassi:";
    public static final String SOMETHING_WENT_WRONG = "C'è stato un problema, riprova più tardi";
}
