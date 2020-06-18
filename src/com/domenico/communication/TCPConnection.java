package com.domenico.communication;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class TCPConnection extends Connection {

    public static final int SERVER_PORT = 5555;
    public static final String SERVER_HOST = "localhost";

    private SocketChannel channel;

    public TCPConnection(SocketChannel channel) {
        this.channel = channel;
    }

    @Override
    void write(ByteBuffer buffer) throws IOException {
        channel.write(buffer);
    }

    @Override
    void read(ByteBuffer buffer) throws IOException {
        channel.read(buffer);
    }

    @Override
    public void endConnection() throws IOException {
        channel.close();
    }
}
