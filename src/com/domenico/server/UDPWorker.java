package com.domenico.server;

import com.domenico.communication.UDPConnection;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;

/**
 * This is a worker class that manages all the UDP communications. It implements channel multiplexing to efficiently
 * manage all the clients by extending the Multiplexer class.
 */
public class UDPWorker extends Multiplexer {

    public UDPWorker(DatagramChannel channel) throws IOException {
        super(channel, SelectionKey.OP_READ, new UDPConnection(channel));
        DatagramSocket datagramSocket = channel.socket();
        datagramSocket.bind(new InetSocketAddress(UDPConnection.PORT));
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
        UDPConnection udpConnection = (UDPConnection) key.attachment();
        client.register(selector, SelectionKey.OP_WRITE, udpConnection);
    }

    /**
     * Called when the method write() will not block the thread
     */
    @Override
    void onWritable(SelectionKey key) throws IOException {
        DatagramChannel client = (DatagramChannel) key.channel();
        UDPConnection udpConnection = (UDPConnection) key.attachment();
        client.register(selector, SelectionKey.OP_READ, udpConnection);
    }

    /** Called when it is needed to close the connection with the endpoint */
    @Override
    void onEndConnection(SelectionKey key) throws IOException {
        UDPConnection udpConnection = (UDPConnection) key.attachment();
        udpConnection.endConnection();
    }

    private void print(String string) {
        System.out.println("[UDP]: "+string);
    }
}
