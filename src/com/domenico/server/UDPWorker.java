package com.domenico.server;

import com.domenico.communication.UDPConnection;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;

public class UDPWorker extends Multiplexer {

    public UDPWorker(DatagramChannel channel) throws IOException {
        super(channel, SelectionKey.OP_READ, new UDPConnection(channel));
        DatagramSocket datagramSocket = channel.socket();
        datagramSocket.bind(new InetSocketAddress(UDPConnection.PORT));
        print("Bound on port " + UDPConnection.PORT);
    }

    @Override
    void onAcceptable(SelectionKey key) {}

    @Override
    void onReadable(SelectionKey key) throws IOException {
        DatagramChannel client = (DatagramChannel) key.channel();
        UDPConnection udpConnection = (UDPConnection) key.attachment();
        client.register(selector, SelectionKey.OP_WRITE, udpConnection);
    }

    @Override
    void onWritable(SelectionKey key) throws IOException {
        DatagramChannel client = (DatagramChannel) key.channel();
        UDPConnection udpConnection = (UDPConnection) key.attachment();
        client.register(selector, SelectionKey.OP_READ, udpConnection);
    }

    private void print(String string) {
        System.out.println("[UDP]: "+string);
    }
}
