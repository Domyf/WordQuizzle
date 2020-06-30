package com.domenico.client;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;

/** Starting point for the client.
 *  The usage can be printed with the --help argument (java MainClassWQClient --help) */
public class MainClassWQClient implements OnChallengeArrivedListener {

    //The usage of this program
    public static final String USAGE = "usage : COMMAND [ ARGS ...]";
    private static final String HELP_COMMAND_ARG = "--help";    //The program argument associated to the usage

    private String loggedUserName = null;
    private RMIClient rmiClient;
    private TCPClient tcpClient;
    private UDPClient udpClient;
    private CLI cli;

    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals(HELP_COMMAND_ARG)) {
            System.out.println(USAGE);
            printCommandsUsage();
            return;
        }

        MainClassWQClient client = new MainClassWQClient();
        client.loop();

    }

    public MainClassWQClient() {
        try {
            rmiClient = new RMIClient();
            tcpClient = new TCPClient();
            udpClient = new UDPClient(this);
            cli = new CLI(new Scanner(System.in));
        } catch (NotBoundException | IOException e) {
            e.printStackTrace();
            return;
        }
    }

    private void loop() {
        boolean run = true;
        while(run) {
            UserCommand userCommand = cli.askForCommand();
            switch (userCommand.getCmd()) {
                case UserCommand.REGISTER_USER:
                    if (userCommand.hasParams(2)) {
                        handleRegister(userCommand.getParam(0), userCommand.getParam(1));
                    } else {
                        System.out.println(Messages.BAD_COMMAND_SINTAX);
                        System.out.println(UserCommand.getCommandUsage(UserCommand.REGISTER_USER));
                    }
                    break;
                case UserCommand.LOGIN:
                    if (userCommand.hasParams(2))
                        handleLogin(userCommand.getParam(0), userCommand.getParam(1));
                    else {
                        System.out.println(Messages.BAD_COMMAND_SINTAX);
                        System.out.println(UserCommand.getCommandUsage(UserCommand.LOGIN));
                    }
                    break;
                case UserCommand.LOGOUT:
                    handleLogout();
                    break;
                case UserCommand.ADD_FRIEND:
                    if (userCommand.hasParams(1))
                        handleAddFriend(userCommand.getParam(0));
                    else {
                        System.out.println(Messages.BAD_COMMAND_SINTAX);
                        System.out.println(UserCommand.getCommandUsage(UserCommand.ADD_FRIEND));
                    }
                    break;
                case UserCommand.FRIEND_LIST:
                    handleShowFriendList();
                    break;
                case UserCommand.CHALLENGE:
                    if (userCommand.hasParams(1))
                        handleSendChallengeRequest(userCommand.getParam(0));
                    else {
                        System.out.println(Messages.BAD_COMMAND_SINTAX);
                        System.out.println(UserCommand.getCommandUsage(UserCommand.CHALLENGE));
                    }
                    break;
                case UserCommand.SHOW_SCORE:
                    handleShowScore();
                    break;
                case UserCommand.SHOW_LEADERBOARD:
                    handleShowLeaderboard();
                    break;
                case UserCommand.EXIT:
                    run = false;
                    if (isLogged()) //If the user is logged in then the logout is done
                        handleLogout();
                    break;
                default:
                    System.out.println(Messages.INVALID_COMMAND);
                    printCommandsUsage();
            }
        }
        try {
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
    private void handleRegister(String username, String password) {
        if (isLogged()) {
            System.out.println(Messages.LOGOUT_NEEDED);
            return;
        }

        try {
            boolean done = rmiClient.register(username, password);
            if (done) {
                boolean shouldLogin = cli.askChoice(Messages.ASK_LOGIN+" (SI/NO oppure S/N)");
                if (shouldLogin)
                    handleLogin(username, password);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** Method invoked when the user types login <nickUtente> <password> */
    private void handleLogin(String username, String password) {
        if (isLogged()) {
            System.out.println(Messages.LOGOUT_NEEDED);
            return;
        }
        try {
            boolean logged = tcpClient.login(username, password, udpClient.getUDPPort());
            if (logged) {
                udpClient.setLoggedUsername(username);
                loggedUserName = username;
                new Thread(udpClient).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Method invoked when the user types logout */
    private void handleLogout() {
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
    private void handleAddFriend(String otherUsername) {
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
    private void handleShowFriendList() {
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
    private void handleSendChallengeRequest(String friendUsername) {
        if (!isLogged()) {
            System.out.println(Messages.LOGIN_NEEDED);
            return;
        }
        // TODO: 17/06/2020 start the game between this user and the one passed as argument
        try {
            tcpClient.challenge(loggedUserName, friendUsername);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Method invoked when the user types mostra_classifica */
    private void handleShowLeaderboard() {
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
    private void handleShowScore() {
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
    private boolean isLogged() {
        return loggedUserName != null;
    }

    @Override
    public boolean onChallengeArrived(String from) {
        return cli.askChoice("\rHai ricevuto una sfida da "+from+". Vuoi accettarla?");
    }
}
