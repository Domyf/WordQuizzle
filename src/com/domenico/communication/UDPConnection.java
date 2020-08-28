package com.domenico.communication;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * Implements a UDP communication with an endpoint. It extends the Connection abstract class.
 */
public class UDPConnection extends Connection {

    public static final int PORT = 9999;
    public static final String HOST_NAME = "localhost";

    private final DatagramChannel channel;    //UDP channel on which the communication is done
    private SocketAddress address;  //The endpoint address

    /**
     * Builds a new UDPConnection object that uses the given channel for the communication between this and the endpoint
     * expressed by the given address.
     * @param channel UDP channel on which the communication is done
     * @param address the endpoint address
     */
    public UDPConnection(DatagramChannel channel, SocketAddress address)  {
        this.channel = channel;
        this.address = address;
    }

    public void sendData(ConnectionData data, SocketAddress address) throws IOException {
        this.address = address;
        sendData(data);
    }

    public SocketAddress getAddress() {
        return address;
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
        if (channel.isOpen())
            channel.close();
    }
}
