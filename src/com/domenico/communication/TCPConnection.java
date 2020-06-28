package com.domenico.communication;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * Implements a TCP communication with an endpoint (which can be a client or a server)
 */
public class TCPConnection {

    public static final int SERVER_PORT = 5555;
    public static final String SERVER_HOST = "localhost";

    private SocketChannel channel;  //The channel on which the TCP communication is done

    /**
     * Instantiates a TCPConnection object that communicates with the other endpoint via the given channel
     * @param channel the channel on which the TCP communication is done
     */
    public TCPConnection(SocketChannel channel) {
        this.channel = channel;
    }

    /**
     * Sends the data given as argument.
     * @param connectionData the data that should be sent
     * @throws IOException if an I/O error occurs
     */
    public void sendData(ConnectionData connectionData) throws IOException {
        String line = connectionData.toString();
        byte[] data = line.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = wrapData(data);
        channel.write(buffer);
    }

    /**
     * Reads and then it returns the received data
     * @return a ConnectionData object that represents the data received. It can be a request or a response.
     * @throws IOException if an I/O error occurs
     */
    public ConnectionData receiveData() throws IOException {
        ByteBuffer lenBuffer = receiveByLength(Integer.BYTES);
        ByteBuffer dataBuffer = receiveByLength(lenBuffer.getInt());
        String line = new String(dataBuffer.array(), StandardCharsets.UTF_8);
        return ConnectionData.Factory.parseLine(line);
    }

    /**
     * Wraps the given data by following the protocol which means that the returned buffer will contain the data's length
     * and then the data itself
     * @param data the data that should be contained inside the buffer
     * @return a ByteBuffer object that contains the data's length followed by the data itself. The buffer is ready to be read.
     */
    private ByteBuffer wrapData(byte[] data) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES+data.length);
        buffer.putInt(data.length);
        buffer.put(data);
        buffer.flip();
        return buffer;
    }

    /**
     * Reads and receive the given amount of bytes
     * @param bufferLength how many bytes should be read
     * @return a buffer with bufferLength size that contains the bytes read
     * @throws IOException if an I/O error occurs
     */
    private ByteBuffer receiveByLength(int bufferLength) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(bufferLength);
        channel.read(buffer);
        buffer.flip();
        return buffer;
    }

    /**
     * Ends the connection with the endpoint
     * @throws IOException if an I/O error occurs
     */
    public void closeConnection() throws IOException {
        channel.close();
    }
}
