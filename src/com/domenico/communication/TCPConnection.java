package com.domenico.communication;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Implements a TCP communication with an endpoint (which can be a client or a server).
 * It extends the Connection abstract class.
 */
public class TCPConnection extends Connection {

    public static final int SERVER_PORT = 5555;
    public static final String SERVER_HOST = "localhost";

    private final SocketChannel channel;  //The channel on which the TCP communication is done

    /**
     * Instantiates a TCPConnection object that communicates with the other endpoint via the given channel
     * @param channel the channel on which the TCP communication is done
     */
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
