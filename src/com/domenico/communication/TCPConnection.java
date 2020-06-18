package com.domenico.communication;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class TCPConnection implements Connection {

    public static final int SERVER_PORT = 5555;
    public static final String SERVER_HOST = "localhost";

    private SocketChannel channel;

    public TCPConnection(SocketChannel channel) {
        this.channel = channel;
    }

    @Override
    public void sendRequest(Request request) throws IOException {
        String requestLine = request.toString();
        write(requestLine.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void sendResponse(Response response) throws IOException {
        String responseLine = response.toString();
        write(responseLine.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Response getResponse() throws IOException {
        String lineRead = read();
        return Response.parseResponse(lineRead);
    }

    @Override
    public Request getRequest() throws IOException {
        String lineRead = read();
        return Request.parseRequest(lineRead);
    }

    @Override
    public void endConnection() throws IOException {
        channel.close();
    }

    /**
     * Sends to dst the data passed to this method. It first sends the length of the data and then the data itself
     * @param data  the bytes that should be sent
     * @throws IOException
     */
    private void write(byte[] data) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES+data.length);
        buffer.putInt(data.length);
        buffer.put(data);
        buffer.flip();
        channel.write(buffer);
    }

    private String read() throws IOException {
        ByteBuffer lengthBuf = read(Integer.BYTES);
        ByteBuffer message = read(lengthBuf.getInt());
        return new String(message.array(), StandardCharsets.UTF_8);
    }

    private ByteBuffer read(int bufferLength) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(bufferLength);
        channel.read(buf);
        buf.flip();
        return buf;
    }
}
