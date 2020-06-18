package com.domenico.communication;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public abstract class Connection {

    public void sendData(ConnectionData connectionData) throws IOException {
        String line = connectionData.toString();
        byte[] data = line.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = wrapData(data);
        write(buffer);
    }

    public ConnectionData receiveData() throws IOException {
        ByteBuffer lenBuffer = receiveByLength(Integer.BYTES);
        ByteBuffer dataBuffer = receiveByLength(lenBuffer.getInt());
        String line = new String(dataBuffer.array(), StandardCharsets.UTF_8);
        return ConnectionData.Factory.parseLine(line);
    }

    private ByteBuffer wrapData(byte[] data) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES+data.length);
        buffer.putInt(data.length);
        buffer.put(data);
        buffer.flip();
        return buffer;
    }

    private ByteBuffer receiveByLength(int bufferLength) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(bufferLength);
        read(buffer);
        buffer.flip();
        return buffer;
    }

    abstract void write(ByteBuffer buffer) throws IOException;
    abstract void read(ByteBuffer buffer) throws IOException;
    abstract void endConnection() throws IOException;
}
