package com.domenico;

import java.util.Scanner;

/** Starting point for the client.
 *  The usage can be printed with the --help argument (java MainClassWQClient --help) */
public class MainClassWQClient {

    private static final String HELP_COMMAND_ARG = "--help";    //The program argument associated to the usage

    //Commands
    private static final String REGISTER_USER = "registra_utente";
    private static final String LOGIN = "login";
    private static final String LOGOUT = "logout";
    private static final String ADD_FRIEND = "aggiungi_amico";
    private static final String FRIEND_LIST = "lista_amici";
    private static final String CHALLENGE = "sfida";
    private static final String SHOW_SCORE = "mostra_punteggio";
    private static final String SHOW_LEADERBOARD = "mostra_classifica";

    //The usage of this program
    private static final String USAGE = "usage : COMMAND [ ARGS ...]\n" +
            "Commands:\n" +
            "   "+REGISTER_USER+" <nickUtente > <password > registra l' utente\n"+
            "   "+LOGIN+" <nickUtente > <password > effettua il login\n" +
            "   "+LOGOUT+" effettua il logout\n" +
            "   "+ADD_FRIEND+" <nickAmico> crea relazione di amicizia con nickAmico\n" +
            "   "+FRIEND_LIST+" mostra la lista dei propri amici\n" +
            "   "+CHALLENGE+" <nickAmico > richiesta di una sfida a nickAmico\n" +
            "   "+SHOW_SCORE+" mostra il punteggio dell’utente\n" +
            "   "+SHOW_LEADERBOARD+" mostra una classifica degli amici dell’utente (incluso l’utente stesso)";

    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals(HELP_COMMAND_ARG)) {
            System.out.println(USAGE);
            return;
        }
        Scanner scanner = new Scanner(System.in);
        while(true) {
            System.out.print("> ");
            String line = scanner.nextLine();
            String[] splittedLine = line.split(" ");
            if (splittedLine.length == 0)
                continue;
            switch (splittedLine[0]) {
                case REGISTER_USER:
                    if (splittedLine.length == 3) {
                        String userName = splittedLine[1];
                        String password = splittedLine[2];
                        System.out.println(password);
                        //TODO Register the user
                    }
                    break;
                case LOGIN:

                    break;
                case LOGOUT:

                    break;
                case ADD_FRIEND:

                    break;
                case FRIEND_LIST:

                    break;
                case CHALLENGE:

                    break;
                case SHOW_SCORE:

                    break;
                case SHOW_LEADERBOARD:

                    break;
            }
        }
    }


}
