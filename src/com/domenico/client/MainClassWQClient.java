package com.domenico.client;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;

/** Starting point for the client.
 *  The usage can be printed with the --help argument (java MainClassWQClient --help) */
public class MainClassWQClient {

    private static final String HELP_COMMAND_ARG = "--help";    //The program argument associated to the usage

    private static Scanner scanner;
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
        scanner = new Scanner(System.in);
        boolean exit = false;
        while(!exit) {
            System.out.print("> ");
            String line = scanner.nextLine();
            Command command = new Command(line);
            switch (command.getCmd()) {
                case Constants.REGISTER_USER:
                    if (command.hasParams(2)) {
                        registerUser(command.getParam(0), command.getParam(1));
                        loginUser(command.getParam(0), command.getParam(1));
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
    private static void registerUser(String username, String password) {
        // TODO: 18/06/2020 Check if the user was registered or not
        try {
            rmiClient.register(username, password);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** Method invoked when the user types login <nickUtente> <password> */
    private static void loginUser(String username, String password) {
        // TODO: 17/06/2020 log in the user to this game
        try {
            tcpClient.login(username, password);
            loggedUserName = "fake";// TODO: 18/06/2020 remove this
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Method invoked when the user types logout */
    private static void logout() {
        if (loggedUserName == null) {
            System.out.println(Messages.LOGIN_NEEDED);
            return;
        }
        // TODO: 17/06/2020 log out the user from this game
        try {
            tcpClient.logout(loggedUserName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Method invoked when the user types aggiungi_amico <nickAmico> */
    private static void addFriend(String otherUsername) {
        if (loggedUserName == null) {
            System.out.println(Messages.LOGIN_NEEDED);
            return;
        }
        // TODO: 17/06/2020 add the given user to this user's friends
        try {
            tcpClient.addFriend(loggedUserName, otherUsername);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Method invoked when the user types mostra_amici */
    private static void showFriendList() {
        if (loggedUserName == null) {
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
        if (loggedUserName == null) {
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
        if (loggedUserName == null) {
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
        if (loggedUserName == null) {
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
}
