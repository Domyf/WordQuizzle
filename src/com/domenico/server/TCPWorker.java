package com.domenico.server;

import com.domenico.communication.*;

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
        client.register(selector, SelectionKey.OP_READ, tcpConnection);
    }

    /** Called when the method read() will not block the thread */
    @Override
    protected void onReadable(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        TCPConnection tcpConnection = (TCPConnection) key.attachment();
        ConnectionData connectionData = tcpConnection.receiveData();
        // TODO: 18/06/2020 parse the data received to understand what response should be sent to the client
        ConnectionData response = parseRequest(connectionData);
        Object[] attachment = {tcpConnection, response};

        client.register(selector, SelectionKey.OP_WRITE, attachment);
    }

    /** Called when the method write() will not block the thread */
    @Override
    protected void onWritable(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        Object[] attachment = (Object[]) key.attachment();
        TCPConnection tcpConnection = (TCPConnection) attachment[0];
        ConnectionData response = (ConnectionData) attachment[1];
        tcpConnection.sendData(response);

        client.register(selector, SelectionKey.OP_READ, tcpConnection);
    }

    private void print(String string) {
        System.out.println("[TCP]: "+string);
    }

    private ConnectionData parseRequest(ConnectionData connectionData) {
        String failMessage = "";
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
            } else if (ConnectionData.Validator.isScoreRequest(connectionData)) {
                String username = connectionData.getUsername();
                // TODO: 26/06/2020 get the score
            } else if (ConnectionData.Validator.isLeaderboardRequest(connectionData)) {
                String username = connectionData.getUsername();
                // TODO: 26/06/2020 get the leaderboard
            }
        } catch (UsersManagementException e) {
            failMessage = e.getMessage();
        }

        if (failMessage.length() == 0)
            return ConnectionData.Factory.newSuccessResponse();

        return ConnectionData.Factory.newFailResponse(failMessage);
    }
}
