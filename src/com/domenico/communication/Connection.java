package com.domenico.communication;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Abstract class that represents a connection between a client and a server or in general between two endpoints.
 * It implements the protocol used by the two parts to communicate. The protocol is the following: first thing sent is
 * the data's length and then the data itself. This class uses ConnectionData objects to represent the data
 * that should be sent or which is received.
 * A subclass of this class should implement the write(), read() and endConnection() method. That methods are dependent
 * of the network protocol used (TCP or UDP for example).
 */
public abstract class Connection {

    /**
     * Sends the data given as argument.
     * @param connectionData the data that should be sent
     * @throws IOException if an I/O error occurs
     */
    public void sendData(ConnectionData connectionData) throws IOException {
        String line = connectionData.toString();
        byte[] data = line.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = wrapData(data);
        write(buffer);
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
        read(buffer);
        buffer.flip();
        return buffer;
    }

    /**
     * Writes the content of the given buffer in order to send it to the endpoint
     * @param buffer the buffer which content should be wrote and sent to the endpoint
     * @throws IOException if an I/O error occurs
     */
    abstract void write(ByteBuffer buffer) throws IOException;

    /**
     * Reads the data from the other endpoint and writes it inside the given buffer.
     * @param buffer the buffer that should contain the data read
     * @throws IOException if an I/O error occurs
     */
    abstract void read(ByteBuffer buffer) throws IOException;

    /**
     * Ends the connection with the endpoint
     * @throws IOException if an I/O error occurs
     */
    abstract void endConnection() throws IOException;
}
