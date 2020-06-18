package com.domenico.communication;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class UDPConnection extends Connection {

    public static final int PORT = 9999;
    public static final String HOST_NAME = "localhost";

    private DatagramChannel channel;
    private SocketAddress address;

    public UDPConnection(DatagramChannel channel, SocketAddress address) {
        this.channel = channel;
        this.address = address;
    }

    public UDPConnection(DatagramChannel channel) {
        this.channel = channel;
    }

    @Override
    void write(ByteBuffer buffer) throws IOException {
        channel.send(buffer, address);
    }

    @Override
    void read(ByteBuffer buffer) throws IOException {
        address = channel.receive(buffer);
    }

    @Override
    public void endConnection() throws IOException {
        channel.close();
    }
}
