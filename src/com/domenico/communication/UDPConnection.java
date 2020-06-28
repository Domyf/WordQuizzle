package com.domenico.communication;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;

/**
 * Implements a UDP communication with an endpoint. It extends the Connection abstract class.
 */
public class UDPConnection {

    public static final int PORT = 9999;
    public static final String HOST_NAME = "localhost";
    private static final int MAX_PACKET_LENGTH = 250;

    private DatagramChannel channel;    //UDP channel on which the communication is done
    private SocketAddress address;  //The endpoint address
    private ByteBuffer buffer;      //The buffer used for all the communications

    /**
     * Builds a new UDPConnection object that uses the given channel for the communication between this and the endpoint
     * expressed by the given address.
     * @param channel UDP channel on which the communication is done
     * @param address the endpoint address
     */
    public UDPConnection(DatagramChannel channel, SocketAddress address) {
        this.channel = channel;
        this.address = address;
        this.buffer = ByteBuffer.allocate(MAX_PACKET_LENGTH);
    }

    /**
     * Builds an UDPConnection object without specifying the endpoint address
     * @param channel the channel on which the UDP communication is done
     */
    public UDPConnection(DatagramChannel channel) {
        this.channel = channel;
        this.buffer = ByteBuffer.allocate(MAX_PACKET_LENGTH);
    }

    public void sendData(ConnectionData connectionData) throws IOException {
        buffer.clear();
        String dataString = connectionData.toString();
        buffer.put(dataString.getBytes(StandardCharsets.UTF_8));
        buffer.flip();
        channel.send(buffer, address);
    }

    public ConnectionData receiveData() throws IOException {
        buffer.clear();
        address = channel.receive(buffer);
        buffer.flip();
        String line = new String(buffer.array(), StandardCharsets.UTF_8);
        return ConnectionData.Factory.parseLine(line);
    }

    public void closeConnection() throws IOException {
        channel.close();
    }
}
