package com.domenico.client;

import org.json.simple.JSONArray;

import java.io.IOException;
import java.rmi.NotBoundException;

public class WQClient implements WQInterface {

    private final RMIClient rmiClient;
    private final TCPClient tcpClient;
    private final UDPClient udpClient;
    private String loggedUserName;

    public WQClient(OnChallengeArrivedListener listener) throws IOException, NotBoundException {
        this.loggedUserName = null;
        this.rmiClient = new RMIClient();
        this.tcpClient = new TCPClient();
        this.udpClient = new UDPClient(this, listener);
        new Thread(udpClient).start();
    }

    @Override
    public boolean onRegisterUser(String username, String password) throws Exception {
        return rmiClient.register(username, password);
    }

    @Override
    public String onLogin(String username, String password) throws Exception {
        if (!isLoggedIn()) {
            String result = tcpClient.login(username, password, udpClient.getUDPPort());
            if (result != null && result.isEmpty()) {
                loggedUserName = username;
                return Messages.LOGIN_SUCCESS;
            }
            return result;
        } else {
            return Messages.LOGOUT_NEEDED;
        }
    }

    @Override
    public String onLogout() throws Exception {
        if (isLoggedIn()) {
            String result = tcpClient.logout(loggedUserName);
            if (result != null && result.isEmpty()) {
                loggedUserName = null;
                return Messages.LOGOUT_SUCCESS;
            } else {
                return result;
            }
        } else {
            return Messages.LOGIN_NEEDED;
        }
    }

    @Override
    public String onAddFriend(String friendUsername) throws Exception {
        if (isLoggedIn()) {
            return tcpClient.addFriend(loggedUserName, friendUsername);
        } else {
            return Messages.LOGIN_NEEDED;
        }
    }

    @Override
    public JSONArray onShowFriendList() throws Exception {
        if (isLoggedIn()) {
            return tcpClient.friendList(loggedUserName);
        } else {
            return null;
        }
    }

    @Override
    public String onSendChallengeRequest(String friendUsername) throws Exception {
        if (isLoggedIn()) {
            return tcpClient.challengeRequest(loggedUserName, friendUsername);
        } else {
            return Messages.LOGIN_NEEDED;
        }
    }

    @Override
    public boolean getChallengeResponse(String friendUsername) throws Exception {
        return tcpClient.challengeResponse();
    }

    @Override
    public String onShowScore() throws Exception {
        if (isLoggedIn()) {
            return tcpClient.showScore(loggedUserName);
        } else {
            return Messages.LOGIN_NEEDED;
        }
    }

    @Override
    public JSONArray onShowLeaderboard() throws Exception {
        if (isLoggedIn()) {
            return tcpClient.friendList(loggedUserName);
        } else {
            return null;
        }
    }

    @Override
    public void onExit() throws Exception {
        udpClient.exit();
    }

    /**
     * @return true if the client's user is logged in
     */
    private boolean isLoggedIn() {
        return loggedUserName != null;
    }

    public String getLoggedUserName() {
        return loggedUserName;
    }
}
