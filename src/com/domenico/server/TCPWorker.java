package com.domenico.server;

import com.domenico.communication.*;
import org.json.simple.JSONArray;

import java.io.IOException;
import java.net.*;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * This is a worker class that manages all the TCP communications. It implements channel multiplexing to efficiently
 * manage all the clients by extending the Multiplexer class.
 */
public class TCPWorker extends Multiplexer implements Runnable {

    private UsersManagement usersManagement;
    private DatagramChannel datagramChannel;
    private Map<String, SelectionKey> mapToKey;

    private class UserRecord {
        TCPConnection tcpConnection;
        ConnectionData response;
        int udpPort;
        InetAddress address;
        FutureTask<ConnectionData> challengePending = null;
    }

    public TCPWorker() throws IOException {
        super(ServerSocketChannel.open(), SelectionKey.OP_ACCEPT);
        ServerSocket serverSocket = ((ServerSocketChannel) channel).socket();
        serverSocket.bind(new InetSocketAddress(TCPConnection.SERVER_PORT));
        print("Listening on port " + TCPConnection.SERVER_PORT);

        this.usersManagement = UsersManagement.getInstance();
        this.datagramChannel = DatagramChannel.open();
        this.mapToKey = new HashMap<>();
        datagramChannel.socket().bind(new InetSocketAddress(UDPConnection.PORT));
    }

    @Override
    public void run() {
        super.startProcessing();
    }

    @Override
    protected void onTimeout() {}

    /** Called when the method accept() will not block the thread */
    @Override
    protected void onAcceptable(SelectionKey key) throws IOException {
        ServerSocketChannel channel = (ServerSocketChannel) key.channel();
        SocketChannel client = channel.accept();
        print("Accepted connection for "+client.getRemoteAddress());
        client.configureBlocking(false);    //non-blocking

        UserRecord attachment = new UserRecord();
        attachment.tcpConnection = new TCPConnection(client);
        attachment.address = client.socket().getInetAddress();
        attachment.response = null;
        attachment.challengePending = null;

        client.register(selector, SelectionKey.OP_READ, attachment);
    }

    /**
     * Called when the method read() will not block the thread
     */
    @Override
    protected void onReadable(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        UserRecord attachment = (UserRecord) key.attachment();
        TCPConnection tcpConnection = attachment.tcpConnection;

        attachment.response = null;
        ConnectionData received = tcpConnection.receiveData();
        print(received.toString());
        try {
            if (ConnectionData.Validator.isLoginRequest(received)) {
                attachment.response = handleLoginRequest(received, attachment, key);

            } else if (ConnectionData.Validator.isLogoutRequest(received)) {
                attachment.response = handleLogoutRequest(received, attachment);

            } else if (ConnectionData.Validator.isAddFriendRequest(received)) {
                attachment.response = handleAddFriendRequest(received);

            } else if (ConnectionData.Validator.isFriendListRequest(received)) {
                attachment.response = handleFriendListRequest(received);

            } else if (ConnectionData.Validator.isChallengeRequest(received)) {
                attachment.challengePending = handleChallengeRequest(received);

            } else if (ConnectionData.Validator.isScoreRequest(received)) {
                attachment.response = handleScoreRequest(received);

            } else if (ConnectionData.Validator.isLeaderboardRequest(received)) {
                attachment.response = handleLeaderboardRequest(received);

            }
        } catch (UsersManagementException e) {
            attachment.response = ConnectionData.Factory.newFailResponse(e.getMessage());
        }

        client.register(selector, SelectionKey.OP_WRITE, attachment);
    }

    private ConnectionData handleLoginRequest(ConnectionData connectionData, UserRecord userRecord, SelectionKey key) throws UsersManagementException {
        String username = connectionData.getUsername();
        String password = connectionData.getPassword();
        usersManagement.login(new User(username, password));
        userRecord.udpPort = Integer.parseUnsignedInt(connectionData.getResponseData());
        mapToKey.put(username, key);

        return ConnectionData.Factory.newSuccessResponse();
    }

    private ConnectionData handleLogoutRequest(ConnectionData received, UserRecord attachment) throws UsersManagementException {
        usersManagement.logout(received.getUsername());
        mapToKey.remove(received.getUsername());
        if (attachment.challengePending != null)
            attachment.challengePending.cancel(true);

        return ConnectionData.Factory.newSuccessResponse();
    }

    private ConnectionData handleAddFriendRequest(ConnectionData received) throws UsersManagementException {
        String username = received.getUsername();
        String friend = received.getFriendUsername();
        usersManagement.addFriend(username, friend);

        return ConnectionData.Factory.newSuccessResponse();
    }

    private ConnectionData handleFriendListRequest(ConnectionData received) throws UsersManagementException {
        List<String> friendList = usersManagement.getFriendList(received.getUsername());
        String jsonString = JSONArray.toJSONString(friendList);

        return ConnectionData.Factory.newSuccessResponse(jsonString);
    }

    private FutureTask<ConnectionData> handleChallengeRequest(ConnectionData received) throws IOException {
        // TODO: 29/06/2020 avoid multiple challenges
        String from = received.getUsername();

        String to = received.getFriendUsername();
        System.out.println("Challenge arrived: "+from+" wants to challenge "+to);
        SelectionKey toKey = mapToKey.get(to);
        UserRecord toRecord = (UserRecord) toKey.attachment();
        SocketAddress toUDPAddress = new InetSocketAddress(toRecord.address, toRecord.udpPort);

        Callable<ConnectionData> callable = new UDPRequest(datagramChannel, toUDPAddress, from, to);
        FutureTask<ConnectionData> futureTask = new FutureTask<>(callable);

        new Thread(futureTask).start();

        return futureTask;
    }

    private ConnectionData handleScoreRequest(ConnectionData received) {
        int score = usersManagement.getScore(received.getUsername());
        // TODO: 26/06/2020 get the real score

        return ConnectionData.Factory.newSuccessResponse(""+score);
    }

    private ConnectionData handleLeaderboardRequest(ConnectionData received) {
        String username = received.getUsername();
        // TODO: 26/06/2020 get the leaderboard

        return ConnectionData.Factory.newSuccessResponse("Leaderboard as not been implemented yet");
    }

    /**
     * Called when the method write() will not block the thread
     */
    @Override
    protected void onWritable(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        UserRecord attachment = (UserRecord) key.attachment();
        TCPConnection tcpConnection = attachment.tcpConnection;

        if (attachment.response != null) {
            print(attachment.response.toString());
            tcpConnection.sendData(attachment.response);

        } else if (attachment.challengePending != null && attachment.challengePending.isDone()) {
            try {
                ConnectionData challengeResponse = attachment.challengePending.get();
                print(challengeResponse.toString());
                tcpConnection.sendData(challengeResponse);

            } catch (InterruptedException | ExecutionException e) {
                tcpConnection.sendData(ConnectionData.Factory.newFailResponse("C'è stato un problema, riprova più tardi"));
            }
        } else {
            return;
        }

        client.register(selector, SelectionKey.OP_READ, attachment);
    }

    /**
     * Called when it is needed to close the connection with the endpoint
     */
    @Override
    void onEndConnection(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        UserRecord attachment = (UserRecord) key.attachment();
        TCPConnection tcpConnection = attachment.tcpConnection;

        //tcpConnection.endConnection();
        print("Ended connection with "+client.getRemoteAddress());
    }

    private void print(String string) {
        System.out.println("[TCP]: "+string);
    }

}
