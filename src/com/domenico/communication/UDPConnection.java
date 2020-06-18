package com.domenico.communication;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;

public class UDPConnection implements Connection {

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
    public void sendRequest(Request request) throws IOException {
        String requestLine = request.toString();
        byte[] bytes = requestLine.getBytes(StandardCharsets.UTF_8);
        send(bytes);
    }

    @Override
    public void sendResponse(Response response) throws IOException {
        String responseLine = response.toString();
        byte[] bytes = responseLine.getBytes(StandardCharsets.UTF_8);
        send(bytes);
    }

    @Override
    public Response getResponse() throws IOException {
        String lineReceived = receive();
        return Response.parseResponse(lineReceived);
    }

    @Override
    public Request getRequest() throws IOException {
        String lineReceived = receive();
        return Request.parseRequest(lineReceived);
    }

    @Override
    public void endConnection() throws IOException {
        channel.close();
    }

    private void send(byte[] data) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES+data.length);
        buffer.putInt(data.length);
        buffer.put(data);
        buffer.flip();
        channel.send(buffer, address);
    }

    private String receive() throws IOException {
        ByteBuffer lenBuffer = receive(Integer.BYTES);
        ByteBuffer dataBuffer = receive(lenBuffer.getInt());
        return new String(dataBuffer.array(), StandardCharsets.UTF_8);
    }

    private ByteBuffer receive(int bufferLength) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(bufferLength);
        address = channel.receive(buffer);
        buffer.flip();
        return buffer;
    }
}
