package com.domenico.server;

import com.domenico.communication.*;
import org.json.simple.JSONArray;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.List;

/**
 * This is a worker class that manages all the TCP communications. It implements channel multiplexing to efficiently
 * manage all the clients by extending the Multiplexer class.
 */
public class TCPWorker extends Multiplexer {

    private UsersManagement usersManagement;

    public TCPWorker(UsersManagement usersManagement) throws IOException {
        super(ServerSocketChannel.open(), SelectionKey.OP_ACCEPT);
        ServerSocket serverSocket = ((ServerSocketChannel) channel).socket();
        serverSocket.bind(new InetSocketAddress(TCPConnection.SERVER_PORT));
        print("Listening on port " + TCPConnection.SERVER_PORT);
        this.usersManagement = usersManagement;
    }

    /** Called when the method accept() will not block the thread */
    @Override
    protected void onAcceptable(SelectionKey key) throws IOException {
        ServerSocketChannel channel = (ServerSocketChannel) key.channel();
        SocketChannel client = channel.accept();
        print("Accepted connection for "+client.getRemoteAddress());
        client.configureBlocking(false);    //non-blocking
        TCPConnection tcpConnection = new TCPConnection(client);
        Object[] attachment = new Object[]{tcpConnection, null};

        client.register(selector, SelectionKey.OP_READ, attachment);
    }

    /**
     * Called when the method read() will not block the thread
     */
    @Override
    protected void onReadable(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        Object[] attachment = (Object[]) key.attachment();  //get the attachment reference
        TCPConnection tcpConnection = (TCPConnection) attachment[0];
        ConnectionData connectionData = tcpConnection.receiveData();

        attachment[1] = parseRequest(connectionData);
        client.register(selector, SelectionKey.OP_WRITE, attachment);
    }

    /**
     * Called when the method write() will not block the thread
     */
    @Override
    protected void onWritable(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        Object[] attachment = (Object[]) key.attachment();  //get the attachment reference
        TCPConnection tcpConnection = (TCPConnection) attachment[0];
        ConnectionData response = (ConnectionData) attachment[1];

        tcpConnection.sendData(response);
        client.register(selector, SelectionKey.OP_READ, attachment);
    }

    /**
     * Called when it is needed to close the connection with the endpoint
     */
    @Override
    void onEndConnection(SelectionKey key) throws IOException {
        Object[] attachment = (Object[]) key.attachment();  //get the attachment reference
        TCPConnection tcpConnection = (TCPConnection) attachment[0];

        tcpConnection.endConnection();
    }

    private void print(String string) {
        System.out.println("[TCP]: "+string);
    }

    private ConnectionData parseRequest(ConnectionData connectionData) {
        try {
            if (ConnectionData.Validator.isLoginRequest(connectionData)) {
                String username = connectionData.getUsername();
                String password = connectionData.getPassword();
                usersManagement.login(new User(username, password));
            } else if (ConnectionData.Validator.isLogoutRequest(connectionData)) {
                usersManagement.logout(connectionData.getUsername());
            } else if (ConnectionData.Validator.isAddFriendRequest(connectionData)) {
                String username = connectionData.getUsername();
                String friend = connectionData.getFriendUsername();
                usersManagement.addFriend(username, friend);
            } else if (ConnectionData.Validator.isFriendListRequest(connectionData)) {
                List<String> friendList = usersManagement.getFriendList(connectionData.getUsername());
                String jsonString = JSONArray.toJSONString(friendList);
                return ConnectionData.Factory.newSuccessResponse(jsonString);
            } else if (ConnectionData.Validator.isScoreRequest(connectionData)) {
                int score = usersManagement.getScore(connectionData.getUsername());
                // TODO: 26/06/2020 get the real score
                return ConnectionData.Factory.newSuccessResponse(""+score);
            } else if (ConnectionData.Validator.isLeaderboardRequest(connectionData)) {
                String username = connectionData.getUsername();
                // TODO: 26/06/2020 get the leaderboard
                return ConnectionData.Factory.newSuccessResponse("Leaderboard as not been implemented yet");
            }
        } catch (UsersManagementException e) {
            return ConnectionData.Factory.newFailResponse(e.getMessage());
        }

        return ConnectionData.Factory.newSuccessResponse();
    }
}
