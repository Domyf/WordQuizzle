package com.domenico.server;

import com.domenico.communication.ConnectionData;
import com.domenico.communication.UDPConnection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.charset.StandardCharsets;

/**
 * This is a worker class that manages all the UDP communications. It implements channel multiplexing to efficiently
 * manage all the clients by extending the Multiplexer class.
 */
public class UDPWorker extends Multiplexer {

    public UDPWorker(DatagramChannel channel) throws IOException {
        super(channel, SelectionKey.OP_READ, null);
        channel.socket().bind(new InetSocketAddress(UDPConnection.HOST_NAME, UDPConnection.PORT));
        print("Bound on port " + UDPConnection.PORT);
    }

    @Override
    void onAcceptable(SelectionKey key) {}

    /**
     * Called when the method read() will not block the thread
     */
    @Override
    void onReadable(SelectionKey key) throws IOException {
        DatagramChannel client = (DatagramChannel) key.channel();
        UDPConnection udpConnection = new UDPConnection(client);
        ByteBuffer buf = ByteBuffer.allocate(250);
        //client.receive(buf);
        buf.flip();
        String received = udpConnection.receiveData().toString();// new String(buf.array(), StandardCharsets.UTF_8);
        print(received);/*
        ConnectionData connectionData = udpConnection.receiveData();
        print(connectionData.toString());
        print(connectionData.getUsername()+" si è connesso");*/

        //client.register(selector, SelectionKey.OP_WRITE, udpConnection);
    }

    /**
     * Called when the method write() will not block the thread
     */
    @Override
    void onWritable(SelectionKey key) throws IOException {
        DatagramChannel client = (DatagramChannel) key.channel();
        UDPConnection udpConnection = (UDPConnection) key.attachment();
        //udpConnection.sendData(ConnectionData.Factory.newSuccessResponse());
        client.register(selector, SelectionKey.OP_WRITE, udpConnection);
    }

    /** Called when it is needed to close the connection with the endpoint */
    @Override
    void onEndConnection(SelectionKey key) throws IOException {
        UDPConnection udpConnection = (UDPConnection) key.attachment();
        udpConnection.closeConnection();
    }

    private void print(String string) {
        System.out.println("[UDP]: "+string);
    }
}
