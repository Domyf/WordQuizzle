package com.domenico.server;

import com.domenico.communication.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * This is a worker class that manages all the TCP communications. It implements channel multiplexing to efficiently
 * manage all the clients.
 */
public class TCPWorker extends Multiplexer {

    public TCPWorker() throws IOException {
        super(ServerSocketChannel.open(), SelectionKey.OP_ACCEPT);
        ServerSocket serverSocket = ((ServerSocketChannel) channel).socket();
        serverSocket.bind(new InetSocketAddress(TCPConnection.SERVER_PORT));
        print("Listening on port " + TCPConnection.SERVER_PORT);
    }

    /** Called when the method accept() will not block the thread */
    @Override
    protected void onClientAcceptable(SelectionKey key) throws IOException {
        ServerSocketChannel channel = (ServerSocketChannel) key.channel();
        SocketChannel client = channel.accept();
        print("Accepted connection for "+client.getRemoteAddress());
        client.configureBlocking(false);    //non-blocking
        TCPConnection tcpConnection = new TCPConnection(client);
        client.register(selector, SelectionKey.OP_READ, tcpConnection);
    }

    /** Called when the method read() will not block the thread */
    @Override
    protected void onClientReadable(SelectionKey key) throws IOException {
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
    protected void onClientWritable(SelectionKey key) throws IOException {
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
        // TODO: 18/06/2020 parse the data received to understand what response should be sent to the client
        if (ConnectionData.Validator.isLoginRequest(connectionData)) {
            String username = connectionData.getParam(0);
            String password = connectionData.getParam(1);
            return ConnectionData.Factory.newFailResponse("Requested to login with username and password: "+username+", "+password);
        } else if (ConnectionData.Validator.isLogoutRequest(connectionData)) {
            String username = connectionData.getParam(0);
            return ConnectionData.Factory.newFailResponse("Requested to logout with username: "+username);
        } else if (ConnectionData.Validator.isAddFriendRequest(connectionData)) {
            String username = connectionData.getParam(0);
            String friend = connectionData.getParam(1);
            return ConnectionData.Factory.newFailResponse("Requested to add friend with username and friend's username: "+username+", "+friend);
        } else if (ConnectionData.Validator.isFriendListRequest(connectionData)) {
            String username = connectionData.getParam(0);
            return ConnectionData.Factory.newFailResponse("Requested to show the friend list with username: "+username);
        } else if (ConnectionData.Validator.isScoreRequest(connectionData)) {
            String username = connectionData.getParam(0);
            return ConnectionData.Factory.newFailResponse("Requested to show the score with username: "+username);
        } else if (ConnectionData.Validator.isLeaderboardRequest(connectionData)) {
            String username = connectionData.getParam(0);
            return ConnectionData.Factory.newFailResponse("Requested to show the leaderboard with username: "+username);
        }
        return ConnectionData.Factory.newFailResponse("Request parsing not implemented yet");
    }
}
