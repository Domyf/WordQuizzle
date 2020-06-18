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
        Request request = tcpConnection.getRequest();
        Response response = parseRequest(request);
        Object[] attachment = {tcpConnection, response};
        client.register(selector, SelectionKey.OP_WRITE, attachment);
    }

    /** Called when the method write() will not block the thread */
    @Override
    protected void onClientWritable(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        Object[] attachment = (Object[]) key.attachment();
        TCPConnection tcpConnection = (TCPConnection) attachment[0];
        Response response = (Response) attachment[1];
        tcpConnection.sendResponse(response);

        client.register(selector, SelectionKey.OP_READ, tcpConnection);
    }

    private void print(String string) {
        System.out.println("[TCP]: "+string);
    }

    private Response parseRequest(Request request) {
        // TODO: 18/06/2020 parse the data received to understand what response should be sent to the client
        if (Request.isLoginRequest(request)) {
            return new SuccessResponse();
        }
        return new FailResponse("Invalid request");
    }
}
