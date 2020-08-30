package com.domenico.client;

import com.domenico.shared.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Scanner;
import java.util.function.Consumer;

public class CLI implements ChallengeListener {

    private static final String COMMAND_LINE_START = "> ";
    private final Scanner scanner;
    private WQInterface wqInterface;
    private final Object mutex = new Object();
    private boolean challengeArrived = false;
    private Consumer<Boolean> onChallengeResponse;

    public CLI() {
        this.scanner = new Scanner(System.in);
    }

    public String getNextLine() {
        System.out.print(COMMAND_LINE_START);
        return scanner.nextLine().trim();
    }

    public boolean askChoice(String message) {
        System.out.println(message+" (SI/NO oppure S/N)");
        String answer = getNextLine().toLowerCase();
        return answer.equals("si") || answer.equals("s");
    }

    @Override
    public void onChallengeArrived(String from, Consumer<Boolean> onResponse) {
        synchronized (mutex) {
            System.out.println("\rArrivata una sfida da "+from+". Vuoi accettare la sfida?");
            System.out.print(COMMAND_LINE_START);
            this.challengeArrived = true;
            this.onChallengeResponse = onResponse;
        }
    }

    @Override
    public void onChallengeArrivedTimeout() {
        synchronized (mutex) {
            challengeArrived = false;
            this.onChallengeResponse = null;
            System.out.println("\rIl tempo è scaduto");
            System.out.print(COMMAND_LINE_START);
        }
    }

    @Override
    public void onChallengeTimeout() {
        System.out.println("\rSfida terminata");
        System.out.print(COMMAND_LINE_START);
    }

    public void loop(WQInterface wqInterface) throws Exception {
        if (wqInterface == null) return;
        this.wqInterface = wqInterface;
        boolean run = true;
        String nextItWord;
        while(run) {
            boolean accepted;
            String line = getNextLine();
            if (line.isBlank()) continue;   //ignore a blank or empty line
            if (!wqInterface.isPlaying()) {
                synchronized (mutex) {
                    if (challengeArrived) {
                        challengeArrived = false;
                        if (line.equals("si") || line.equals("s")) {
                            onChallengeResponse.accept(true);
                            nextItWord = startChallenge();
                        } else {
                            onChallengeResponse.accept(false);
                        }
                        continue;   //message was processed so go next loop cycle
                    }
                }
            } else {
                //TODO send the translated word and wait for the next
                System.out.println("Invio la traduzione ed ottengo la successiva");
                System.out.printf("Challenge %d/%d: %s\n",
                        wqInterface.getWordCounter(), wqInterface.getChallengeWords(), "NEXT");
                continue;
            }
            UserCommand userCommand = new UserCommand(line);
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
                        accepted = handleSendChallengeRequest(userCommand.getParam(0));
                        if (accepted) {
                            startChallenge();
                        }
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

    /** Method invoked when the challenge starts */
    private String startChallenge() throws Exception {
        System.out.println("Via alla sfida di traduzione!");
        String nextItWord = wqInterface.onChallengeStart();
        if (nextItWord.isEmpty()) {
            System.out.println(Messages.SOMETHING_WENT_WRONG);
            return "";
        }
        System.out.printf("Avete %d secondi per tradurre correttamente %d parole.\n",
                wqInterface.getChallengeLength(), wqInterface.getChallengeWords());
        System.out.printf("Challenge %d/%d: %s\n",
                wqInterface.getWordCounter(), wqInterface.getChallengeWords(), nextItWord);
        return nextItWord;
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

    /** Method invoked when the user types sfida <nickAmico>
     * @return true if the challenge has been accepted, false otherwise
     * */
    private boolean handleSendChallengeRequest(String friendUsername) throws Exception {
        String result = wqInterface.onSendChallengeRequest(friendUsername);
        if (result == null) {
            System.out.println(Messages.SOMETHING_WENT_WRONG);
        } else if (result.isEmpty()) {
            System.out.println("Sfida inviata a "+friendUsername+". In attesa di risposta...");
            StringBuffer response = new StringBuffer();
            boolean accepted = wqInterface.waitChallengeResponse(response);
            System.out.println(response);
            return accepted;
        } else {
            System.out.println(result);
        }
        return false;
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
        JSONObject leaderboard = wqInterface.onShowLeaderboard();
        if (leaderboard == null) {
            System.out.println(Messages.SOMETHING_WENT_WRONG);
        } else {    //print the leaderboard
            int left_space = String.valueOf(leaderboard.size()).length();   //how much space should be left
            if (!leaderboard.isEmpty()) {   //print header
                System.out.printf(" %" + left_space + "s%10s%7s%s\n", "", "Utente", "", "Punteggio");
                int pos = 1;
                for (Object key : leaderboard.keySet()) {
                    String points = leaderboard.get(key).toString();
                    System.out.printf("#%-" + left_space + "d", pos);   //print position
                    System.out.print(Utils.getCenteredString((String) key, 15));    //print username
                    System.out.println(Utils.getCenteredString(points, 14));    //print points and then new line
                    pos++;
                }
            }
        }
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
