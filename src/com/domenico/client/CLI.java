package com.domenico.client;

import com.domenico.shared.Utils;
import org.json.simple.JSONArray;

import java.util.Scanner;

public class CLI implements OnChallengeArrivedListener {

    private static final String COMMAND_LINE_START = "> ";
    private Scanner scanner;
    private WQInterface wqInterface;
    private String lastInput = null;
    private final Object mutex = new Object();
    private Boolean challengeArrived = false;

    public CLI() throws Exception {
        this.scanner = new Scanner(System.in);
        this.wqInterface = new WQClient(this);
    }

    public synchronized String getNextLine() {
        if (lastInput == null) {
            System.out.print(COMMAND_LINE_START);
            lastInput = scanner.nextLine().trim();
        }
        return lastInput;
    }

    public UserCommand askForCommand() throws InterruptedException {
        UserCommand uc = null;
        synchronized (mutex) {  //Se sto gestendo l'arrivo della sfida allora attendo
            while (challengeArrived) {
                mutex.wait();
            }
        }
        String line = getNextLine();
        synchronized (mutex) {
            //Ritorno il comando solo se la sfida non è arrivata
            if (!challengeArrived && !line.isBlank()) {
                uc = new UserCommand(line);
                lastInput = null;
            } else if (line.isBlank()) {
                lastInput = null;
            }
        }
        return uc;
    }

    public boolean askChoice(String message) {
        System.out.println(message+" (SI/NO oppure S/N)");
        String answer = getNextLine().toLowerCase();
        lastInput = null;
        return answer.equals("si") || answer.equals("s");
    }

    @Override
    public boolean onChallengeArrived(String from) {
        System.out.println("\rArrivata una sfida da "+from+". Vuoi accettare la sfida?");
        System.out.print(COMMAND_LINE_START);
        synchronized (mutex) {
            challengeArrived = true;
        }
        String answer = getNextLine().trim().toLowerCase();
        synchronized (mutex) {
            challengeArrived = false;
            lastInput = null;
            mutex.notify();
        }
        return answer.equals("si") || answer.equals("s");
    }

    public void loop() throws Exception {
        boolean run = true;
        while(run) {
            UserCommand userCommand = askForCommand();
            if (userCommand == null) continue;
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
                    if (wqInterface.isLoggedIn())
                        handleLogout();
                    break;
                default:
                    System.out.println(Messages.INVALID_COMMAND);
                    CLI.printCommandsUsage();
            }
        }
        wqInterface.onExit();
    }

    /** Method invoked when the user types a command with bad syntax */
    private void onBadCommandSintax(String commandUsage) {
        System.out.println(Messages.BAD_COMMAND_SINTAX);
        System.out.println(commandUsage);
    }

    /** Method invoked when the user types registra_utente <nickUtente> <password> */
    private void handleRegister(String username, String password) throws Exception {
        String response = wqInterface.onRegisterUser(username, password);
        if (response == null) {
            System.out.println(Messages.SOMETHING_WENT_WRONG);
        } else {
            if (response.isEmpty())
                System.out.println(Messages.REGISTRATION_SUCCESS);
            else
                System.out.println(response);
            if (response.isEmpty() && askChoice(Messages.ASK_LOGIN)) {
                handleLogin(username, password);
            }
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
}
