package com.domenico.server.network;

import com.domenico.communication.ConnectionData;
import com.domenico.communication.TCPConnection;
import com.domenico.server.usersmanagement.UsersManagementException;
import com.domenico.server.WQHandler;
import com.domenico.shared.Multiplexer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

/** This class extends the {@link Multiplexer} class and it handles all the TCP communications from and to the clients.
 * When a message arrives, call the right handler's method and leaves it to the rest of the job. The handler sometimes
 * can return a message that should be sent back to the client. Otherwise this class also implements the functionalities
 * to send async messages to the clients.
 */
public class TCPServer extends Multiplexer {

    //The handler that will handle the received message
    private final WQHandler handler;
    //A list, protected by its mutex, that contains each message that should be sent to a defined client
    private final Object mutex = new Object();
    private final LinkedList<ASyncResponse> aSyncResponses = new LinkedList<>();

    /** Inner class that represent an async message that should be sent to a specified client */
    private static class ASyncResponse {
        ConnectionData data;
        SelectionKey key;

        public ASyncResponse(ConnectionData data, SelectionKey key) {
            this.data = data;
            this.key = key;
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

        UserAttachment attachment = new UserAttachment(client);
        client.register(selector, SelectionKey.OP_READ, attachment);
    }

    /**
     * Called when the method read() will not block the thread
     */
    @Override
    protected void onReadable(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        UserAttachment attachment = (UserAttachment) key.attachment();
        TCPConnection tcpConnection = attachment.getTcpConnection();

        ConnectionData received = tcpConnection.receiveData();
        try {
            ConnectionData response = null;
            if (ConnectionData.Validator.isLoginRequest(received)) {
                response = handler.handleLoginRequest(received, key, client.socket().getInetAddress());

            } else if (ConnectionData.Validator.isLogoutRequest(received)) {
                response = handler.handleLogoutRequest(received);

            } else if (ConnectionData.Validator.isAddFriendRequest(received)) {
                response = handler.handleAddFriendRequest(received);

            } else if (ConnectionData.Validator.isFriendListRequest(received)) {
                response = handler.handleFriendListRequest(received);

            } else if (ConnectionData.Validator.isChallengeRequest(received)) {
                response = handler.handleChallengeRequest(received, key);

            } else if (ConnectionData.Validator.isChallengeWord(received)) {
                response = handler.handleTranslationArrived(received, key);

            } else if (ConnectionData.Validator.isScoreRequest(received)) {
                response = handler.handleScoreRequest(received);

            } else if (ConnectionData.Validator.isLeaderboardRequest(received)) {
                response = handler.handleLeaderboardRequest(received);

            }
            attachment.setResponse(response);
        } catch (UsersManagementException e) {
            attachment.setResponse(ConnectionData.Factory.newFailResponse(e.getMessage()));
        }
        print(received.toString(), "<-", client.getRemoteAddress(), attachment.getUsername());
        //If the response is already available then go write it, otherwise stay interested on read
        if (attachment.getResponse() != null)
            client.register(selector, SelectionKey.OP_WRITE, attachment);
    }

    /** Called when the method write() will not block the thread */
    @Override
    protected void onWritable(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        UserAttachment attachment = (UserAttachment) key.attachment();
        TCPConnection tcpConnection = attachment.getTcpConnection();
        ConnectionData response = attachment.getResponse();

        if (response != null) {
            print(response.toString(), "->", client.getRemoteAddress(), attachment.getUsername());
            tcpConnection.sendData(response);
            attachment.setResponse(null);
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
                UserAttachment attachment = (UserAttachment) aSyncResponse.key.attachment();
                attachment.setResponse(aSyncResponse.data);
                if (aSyncResponse.key.isValid())
                    aSyncResponse.key.interestOps(SelectionKey.OP_WRITE);
            }
        }
    }

    /**
     * Sends the given message to the given client.
     * @param data the message that should be sent
     * @param key the key that represent the client
     */
    public void sendToClient(ConnectionData data, SelectionKey key) {
        synchronized (mutex) {
            aSyncResponses.push(new ASyncResponse(data, key));
            super.wakeUp();
        }
    }

    /** Print the message arrived or that was sent */
    private void print(String message, String direction, SocketAddress toAddress, String username) {
        System.out.printf("[TCP]: %s %s %s (%s)\n", message, direction, toAddress, username);
    }

    /** Print a generic message */
    private void print(String str) {
        System.out.println("[TCP]: "+str);
    }

    @Override
    protected void onTimeout() {}   //never invoked because the select() has no timeout in this thread
}
