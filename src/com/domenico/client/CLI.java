package com.domenico.client;

import com.domenico.shared.Utils;
import org.json.simple.JSONArray;

import java.util.Scanner;

public class CLI implements OnChallengeArrivedListener {

    private static final String COMMAND_LINE_START = "> ";
    private Scanner scanner;
    private WQInterface wqInterface;

    public CLI() throws Exception {
        this.scanner = new Scanner(System.in);
        this.wqInterface = new WQClient(this);
    }

    public synchronized UserCommand askForCommand() {
        System.out.print(COMMAND_LINE_START);
        String line = scanner.nextLine().trim();
        if (line.isBlank())
            return null;

        return new UserCommand(line);
    }

    public synchronized boolean askChoice(String message) {
        System.out.println(message);
        System.out.print(COMMAND_LINE_START);
        String answer = scanner.nextLine();

        answer = answer.trim().toLowerCase();

        return answer.equals("si") || answer.equals("s");
    }

    public void loop() throws Exception {
        boolean run = true;
        while(run) {
            UserCommand userCommand = askForCommand();
            switch (userCommand.getCmd().toLowerCase()) {
                case UserCommand.REGISTER_USER:
                    if (userCommand.hasParams(2)) {
                        handleRegister(userCommand.getParam(0), userCommand.getParam(1));
                    } else {
                        onBadCommandSintax(UserCommand.getCommandUsage(UserCommand.REGISTER_USER));
                    }
                    break;
                case UserCommand.LOGIN:
                    if (userCommand.hasParams(2)) {
                        handleLogin(userCommand.getParam(0), userCommand.getParam(1));
                    } else {
                        onBadCommandSintax(UserCommand.getCommandUsage(UserCommand.LOGIN));
                    }
                    break;
                case UserCommand.LOGOUT:
                    handleLogout();
                    break;
                case UserCommand.ADD_FRIEND:
                    if (userCommand.hasParams(1)) {
                        handleAddFriend(userCommand.getParam(0));
                    } else {
                        onBadCommandSintax(UserCommand.getCommandUsage(UserCommand.ADD_FRIEND));
                    }
                    break;
                case UserCommand.FRIEND_LIST:
                    handleShowFriendList();
                    break;
                case UserCommand.CHALLENGE:
                    if (userCommand.hasParams(1)) {
                        handleSendChallengeRequest(userCommand.getParam(0));
                    } else {
                        onBadCommandSintax(UserCommand.getCommandUsage(UserCommand.CHALLENGE));
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
                    wqInterface.onLogout();
                    break;
                default:
                    System.out.println(Messages.INVALID_COMMAND);
                    CLI.printCommandsUsage();
            }
        }
        wqInterface.onExit();
    }

    private void onBadCommandSintax(String commandUsage) {
        System.out.println(Messages.BAD_COMMAND_SINTAX);
        System.out.println(commandUsage);
    }

    /** Method invoked when the user types registra_utente <nickUtente> <password> */
    private void handleRegister(String username, String password) throws Exception {
        boolean done = wqInterface.onRegisterUser(username, password);
        if (done) {
            if (askChoice(Messages.ASK_LOGIN+" (SI/NO oppure S/N)"))
                handleLogin(username, password);
        }
    }

    /** Method invoked when the user types login <nickUtente> <password> */
    private void handleLogin(String username, String password) throws Exception {
        String response = wqInterface.onLogin(username, password);
        if (response == null)
            System.out.println(Messages.SOMETHING_WENT_WRONG);
        else
            System.out.println(response);
    }

    /** Method invoked when the user types logout */
    private void handleLogout() throws Exception {
        String response = wqInterface.onLogout();
        if (response == null)
            System.out.println(Messages.SOMETHING_WENT_WRONG);
        else
            System.out.println(response);
    }

    /** Method invoked when the user types aggiungi_amico <nickAmico> */
    private void handleAddFriend(String friendUsername) throws Exception {
        String response = wqInterface.onAddFriend(friendUsername);
        if (response == null)
            System.out.println(Messages.SOMETHING_WENT_WRONG);
        else
            System.out.println(response);
    }

    /** Method invoked when the user types mostra_amici */
    private void handleShowFriendList() throws Exception {
        JSONArray friends = wqInterface.onShowFriendList();
        if (friends == null) {
            System.out.println(Messages.SOMETHING_WENT_WRONG);
        } else if (friends.isEmpty()) {
            System.out.println("Non hai nessun amico");
        } else {
            System.out.print("I tuoi amici sono: ");
            String friendList = Utils.stringify(friends, ", ");
            System.out.println(friendList);
        }
    }

    /** Method invoked when the user types sfida <nickAmico> */
    private void handleSendChallengeRequest(String friendUsername) throws Exception {
        String result = wqInterface.onSendChallengeRequest(friendUsername);
        if (result == null) {
            System.out.println(Messages.SOMETHING_WENT_WRONG);
        } else if (result.isEmpty()) {
            System.out.println("Sfida inviata a "+friendUsername+". In attesa di risposta...");
            boolean accepted = wqInterface.getChallengeResponse(friendUsername);
            if (accepted) {
                System.out.println(friendUsername + " ha accettato la sfida");
            } else {
                System.out.println(friendUsername + " ha rifiutato la sfida");
            }
        } else {
            System.out.println(result);
        }
    }

    /** Method invoked when the user types mostra_punteggio */
    private void handleShowScore() throws Exception {
        String response = wqInterface.onShowScore();
        if (response == null)
            System.out.println(Messages.SOMETHING_WENT_WRONG);
        else
            System.out.println(response);
    }

    /** Method invoked when the user types mostra_classifica */
    private void handleShowLeaderboard() throws Exception {
        JSONArray leaderboard = wqInterface.onShowLeaderboard();
        //TODO print the leaderboard
        if (leaderboard == null)
            System.out.println(Messages.SOMETHING_WENT_WRONG);
        else
            System.out.println("Stamperò la leaderboard un giorno");
    }

    /**
     * Prints each command's usage
     */
    public static void printCommandsUsage() {
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

    @Override
    public boolean onChallengeArrived(String from) {
        return true;
    }
}
