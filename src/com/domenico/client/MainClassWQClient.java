package com.domenico.client;

import com.domenico.server.User;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;

/** Starting point for the client.
 *  The usage can be printed with the --help argument (java MainClassWQClient --help) */
public class MainClassWQClient {

    //The usage of this program
    public static final String USAGE = "usage : COMMAND [ ARGS ...]";
    private static final String HELP_COMMAND_ARG = "--help";    //The program argument associated to the usage

    private static String loggedUserName = null;
    private static RMIClient rmiClient;
    private static TCPClient tcpClient;
    private static UDPClient udpClient;

    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals(HELP_COMMAND_ARG)) {
            System.out.println(USAGE);
            printCommandsUsage();
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
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        while(!exit) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            UserCommand userCommand = new UserCommand(line);
            if (line.isBlank()) continue;
            switch (userCommand.getCmd()) {
                case UserCommand.REGISTER_USER:
                    if (userCommand.hasParams(2)) {
                        boolean registered = registerUser(userCommand.getParam(0), userCommand.getParam(1));
                        if (registered) {
                            System.out.println(Messages.ASK_LOGIN+" (SI/NO oppure S/N)");
                            System.out.print("> ");
                            String answer = scanner.nextLine();
                            answer = answer.trim().toLowerCase();
                            if (answer.equals("si") || answer.equals("s"))
                                loginUser(userCommand.getParam(0), userCommand.getParam(1));
                        }
                    } else {
                        System.out.println(Messages.BAD_COMMAND_SINTAX);
                        System.out.println(UserCommand.getCommandUsage(UserCommand.REGISTER_USER));
                    }
                    break;
                case UserCommand.LOGIN:
                    if (userCommand.hasParams(2))
                        loginUser(userCommand.getParam(0), userCommand.getParam(1));
                    else {
                        System.out.println(Messages.BAD_COMMAND_SINTAX);
                        System.out.println(UserCommand.getCommandUsage(UserCommand.LOGIN));
                    }
                    break;
                case UserCommand.LOGOUT:
                    logout();
                    break;
                case UserCommand.ADD_FRIEND:
                    if (userCommand.hasParams(1))
                        addFriend(userCommand.getParam(0));
                    else {
                        System.out.println(Messages.BAD_COMMAND_SINTAX);
                        System.out.println(UserCommand.getCommandUsage(UserCommand.ADD_FRIEND));
                    }
                    break;
                case UserCommand.FRIEND_LIST:
                    showFriendList();
                    break;
                case UserCommand.CHALLENGE:
                    if (userCommand.hasParams(1))
                        startGame(userCommand.getParam(0));
                    else {
                        System.out.println(Messages.BAD_COMMAND_SINTAX);
                        System.out.println(UserCommand.getCommandUsage(UserCommand.CHALLENGE));
                    }
                    break;
                case UserCommand.SHOW_SCORE:
                    showScore();
                    break;
                case UserCommand.SHOW_LEADERBOARD:
                    showLeaderboard();
                    break;
                case UserCommand.EXIT:
                    exit = true;
                    break;
                default:
                    System.out.println(Messages.INVALID_COMMAND);
                    printCommandsUsage();
            }
        }
        try {
            tcpClient.exit();
            udpClient.exit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Prints the usage of each command
     */
    private static void printCommandsUsage() {
        System.out.println("Commands:\n" +
                "   "+UserCommand.getCommandUsage(UserCommand.REGISTER_USER)+"\n"+
                "   "+UserCommand.getCommandUsage(UserCommand.LOGIN)+"\n" +
                "   "+UserCommand.getCommandUsage(UserCommand.LOGOUT)+"\n" +
                "   "+UserCommand.getCommandUsage(UserCommand.ADD_FRIEND)+"\n" +
                "   "+UserCommand.getCommandUsage(UserCommand.FRIEND_LIST)+"\n" +
                "   "+UserCommand.getCommandUsage(UserCommand.CHALLENGE)+"\n" +
                "   "+UserCommand.getCommandUsage(UserCommand.SHOW_SCORE)+"\n" +
                "   "+UserCommand.getCommandUsage(UserCommand.SHOW_LEADERBOARD)+"\n" +
                "   "+UserCommand.getCommandUsage(UserCommand.EXIT));
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

    /**
     * @return true if the client's user is logged in
     */
    private static boolean isLogged() {
        return loggedUserName != null;
    }
}
