package com.domenico.client;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;

/** Starting point for the client.
 *  The usage can be printed with the --help argument (java MainClassWQClient --help) */
public class MainClassWQClient {

    private static final String HELP_COMMAND_ARG = "--help";    //The program argument associated to the usage

    private static String loggedUserName;
    private static RMIClient rmiClient;
    private static TCPClient tcpClient;
    private static UDPClient udpClient;

    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals(HELP_COMMAND_ARG)) {
            System.out.println(Constants.USAGE);
            return;
        }

        try {
            rmiClient = new RMIClient();
            tcpClient = new TCPClient();
            udpClient = new UDPClient();
        } catch (NotBoundException | IOException e) {
            e.printStackTrace();
            return;
        }
        loggedUserName = null;
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        while(!exit) {
            System.out.print("> ");
            String line = scanner.nextLine();
            Command command = new Command(line);
            switch (command.getCmd()) {
                case Constants.REGISTER_USER:
                    if (command.hasParams(2)) {
                        boolean done = registerUser(command.getParam(0), command.getParam(1));
                        if (done) {
                            System.out.println("Vuoi eseguire il log in? (SI/NO oppure S/N)");
                            System.out.print("> ");
                            String answer = scanner.nextLine();
                            answer = answer.trim().toLowerCase();
                            if (answer.equals("si") || answer.equals("s"))
                                loginUser(command.getParam(0), command.getParam(1));
                        }
                    }
                    break;
                case Constants.LOGIN:
                    if (command.hasParams(2))
                        loginUser(command.getParam(0), command.getParam(1));
                    break;
                case Constants.LOGOUT:
                    logout();
                    break;
                case Constants.ADD_FRIEND:
                    if (command.hasParams(1))
                        addFriend(command.getParam(0));
                    break;
                case Constants.FRIEND_LIST:
                    showFriendList();
                    break;
                case Constants.CHALLENGE:
                    if (command.hasParams(1))
                        startGame(command.getParam(0));
                    break;
                case Constants.SHOW_SCORE:
                    showScore();
                    break;
                case Constants.SHOW_LEADERBOARD:
                    showLeaderboard();
                    break;
                case Constants.EXIT:
                    exit = true;
                    break;
            }
        }
        try {
            tcpClient.exit();
            udpClient.exit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Method invoked when the user types registra_utente <nickUtente> <password> */
    private static boolean registerUser(String username, String password) {
        if (isLogged()) {
            System.out.println(Messages.LOGOUT_NEEDED);
            return false;
        }
        boolean done = false;
        try {
            done = rmiClient.register(username, password);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return done;
    }

    /** Method invoked when the user types login <nickUtente> <password> */
    private static void loginUser(String username, String password) {
        if (isLogged()) {
            System.out.println(Messages.LOGOUT_NEEDED);
            return;
        }
        try {
            boolean logged = tcpClient.login(username, password);
            if (logged)
                loggedUserName = username;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Method invoked when the user types logout */
    private static void logout() {
        if (!isLogged()) {
            System.out.println(Messages.LOGIN_NEEDED);
            return;
        }
        try {
            tcpClient.logout(loggedUserName);
            loggedUserName = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Method invoked when the user types aggiungi_amico <nickAmico> */
    private static void addFriend(String otherUsername) {
        if (!isLogged()) {
            System.out.println(Messages.LOGIN_NEEDED);
            return;
        }
        try {
            tcpClient.addFriend(loggedUserName, otherUsername);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Method invoked when the user types mostra_amici */
    private static void showFriendList() {
        if (!isLogged()) {
            System.out.println(Messages.LOGIN_NEEDED);
            return;
        }
        // TODO: 17/06/2020 show the user's friends
        try {
            tcpClient.friendList(loggedUserName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Method invoked when the user types sfida <nickAmico> */
    private static void startGame(String friendUsername) {
        if (!isLogged()) {
            System.out.println(Messages.LOGIN_NEEDED);
            return;
        }
        // TODO: 17/06/2020 start the game between this user and the one passed as argument
        try {
            udpClient.startGame(loggedUserName, friendUsername);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Method invoked when the user types mostra_classifica */
    private static void showLeaderboard() {
        if (!isLogged()) {
            System.out.println(Messages.LOGIN_NEEDED);
            return;
        }
        // TODO: 17/06/2020 print the leaderboard
        try {
            tcpClient.showLeaderboard(loggedUserName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Method invoked when the user types mostra_punteggio */
    private static void showScore() {
        if (!isLogged()) {
            System.out.println(Messages.LOGIN_NEEDED);
            return;
        }
        // TODO: 17/06/2020 print the user's score
        try {
            tcpClient.showScore(loggedUserName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean isLogged() {
        return loggedUserName != null;
    }
}
