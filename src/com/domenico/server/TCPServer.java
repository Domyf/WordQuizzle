package com.domenico.server;

import com.domenico.communication.ConnectionData;
import com.domenico.communication.TCPConnection;
import com.domenico.shared.Multiplexer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

public class TCPServer extends Multiplexer {

    private final WQHandler handler;
    private final Object mutex = new Object();
    private final LinkedList<ASyncResponse> aSyncResponses = new LinkedList<>();

    private static class ASyncResponse {
        ConnectionData data;
        SelectionKey key;

        public ASyncResponse(ConnectionData data, SelectionKey key) {
            this.data = data;
            this.key = key;
        }
    }

    public static class Attachment {
        String username;
        Challenge challenge;
        InetSocketAddress address;
        ConnectionData response;
        TCPConnection tcpConnection;

        public Attachment(SocketChannel client) {
            this.tcpConnection = new TCPConnection(client);
            this.response = null;
        }
    }

    public TCPServer(WQHandler handler) throws IOException {
        super(ServerSocketChannel.open(), SelectionKey.OP_ACCEPT);
        ServerSocket serverSocket = ((ServerSocketChannel) channel).socket();
        serverSocket.bind(new InetSocketAddress(TCPConnection.SERVER_PORT));
        print("Listening on port " + TCPConnection.SERVER_PORT);
        this.handler = handler;
    }

    /** Called when the method accept() will not block the thread */
    @Override
    protected void onAcceptable(SelectionKey key) throws IOException {
        ServerSocketChannel channel = (ServerSocketChannel) key.channel();
        SocketChannel client = channel.accept();
        print("Accepted connection for "+client.getRemoteAddress());
        client.configureBlocking(false);    //non-blocking

        Attachment attachment = new Attachment(client);
        client.register(selector, SelectionKey.OP_READ, attachment);
    }

    /**
     * Called when the method read() will not block the thread
     */
    @Override
    protected void onReadable(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        Attachment attachment = (Attachment) key.attachment();
        TCPConnection tcpConnection = attachment.tcpConnection;

        ConnectionData received = tcpConnection.receiveData();
        try {
            if (ConnectionData.Validator.isLoginRequest(received)) {
                attachment.response = handler.handleLoginRequest(received, key, client.socket().getInetAddress());

            } else if (ConnectionData.Validator.isLogoutRequest(received)) {
                attachment.response = handler.handleLogoutRequest(received);

            } else if (ConnectionData.Validator.isAddFriendRequest(received)) {
                attachment.response = handler.handleAddFriendRequest(received);

            } else if (ConnectionData.Validator.isFriendListRequest(received)) {
                attachment.response = handler.handleFriendListRequest(received);

            } else if (ConnectionData.Validator.isChallengeRequest(received)) {
                attachment.response = handler.handleChallengeRequest(received, key);

            } else if (ConnectionData.Validator.isScoreRequest(received)) {
                attachment.response = handler.handleScoreRequest(received);

            } else if (ConnectionData.Validator.isLeaderboardRequest(received)) {
                attachment.response = handler.handleLeaderboardRequest(received);

            } else {
                attachment.response = null;
            }
        } catch (UsersManagementException e) {
            attachment.response = ConnectionData.Factory.newFailResponse(e.getMessage());
        }
        print(received.toString(), "<-", client.getRemoteAddress(), attachment.username);
        //If the response is already available then go write it, otherwise stay interested on read
        if (attachment.response != null)
            client.register(selector, SelectionKey.OP_WRITE, attachment);
    }

    /** Called when the method write() will not block the thread */
    @Override
    protected void onWritable(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        Attachment attachment = (Attachment) key.attachment();
        TCPConnection tcpConnection = attachment.tcpConnection;

        if (attachment.response != null) {
            print(attachment.response.toString(), "->", client.getRemoteAddress(), attachment.username);
            tcpConnection.sendData(attachment.response);
            attachment.response = null;
            key.interestOps(SelectionKey.OP_READ);
        }
    }

    /**
     * Called when the connection with a client is closed
     */
    @Override
    protected void onEndConnection(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();

        handler.handleUserDisconnected(key);

        print("Ended connection with "+client.getRemoteAddress());
    }

    @Override
    protected void onWakeUp() {
        synchronized (mutex) {
            //If there is at least a request to forward, then go write into the socket
            while (!aSyncResponses.isEmpty()) {
                ASyncResponse aSyncResponse = aSyncResponses.pop();
                Attachment attachment = (Attachment) aSyncResponse.key.attachment();
                attachment.response = aSyncResponse.data;
                if (aSyncResponse.key.isValid())
                    aSyncResponse.key.interestOps(SelectionKey.OP_WRITE);
            }
        }
    }

    public void sendToClient(ConnectionData data, SelectionKey key) {
        synchronized (mutex) {
            aSyncResponses.push(new ASyncResponse(data, key));
            super.wakeUp();
        }
    }

    public void print(String received, String direction, SocketAddress toAddress, String username) {
        System.out.printf("[TCP]: %s %s %s (%s)\n", received, direction, toAddress, username);
    }

    public void print(String str) {
        System.out.println("[TCP]: "+str);
    }

    @Override
    protected void onTimeout() {}   //never invoked because the select() has no timeout in this thread
}
