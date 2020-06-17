package com.domenico;

public class MainClassWQClient {
    private static final String HELP_COMMAND_ARG = "--help";    //The program argument associated to the usage
    //The usage of this program
    private static final String USAGE = "usage : COMMAND [ ARGS ...]\n" +
            "Commands:\n" +
            "   registra_utente <nickUtente > <password > registra l' utente\n" +
            "   login <nickUtente > <password > effettua il login\n" +
            "   logout effettua il logout\n" +
            "   aggiungi_amico <nickAmico> crea relazione di amicizia con nickAmico\n" +
            "   lista_amici mostra la lista dei propri amici\n" +
            "   sfida <nickAmico > richiesta di una sfida a nickAmico\n" +
            "   mostra_punteggio mostra il punteggio dell’utente\n" +
            "   mostra_classifica mostra una classifica degli amici dell’utente (incluso l’utente stesso)";

    ;
    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals(HELP_COMMAND_ARG)) {
            System.out.println(USAGE);
            return;
        }

    }
}
