package com.domenico.client;

import com.domenico.communication.ConnectionData;
import com.domenico.communication.TCPConnection;
import com.domenico.shared.Multiplexer;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class TCPMultiplexer extends Multiplexer implements Runnable {

    private final TCPConnection tcpConnection;
    private final Consumer<ConnectionData> listener;
    private final Object mutex = new Object();
    private final LinkedList<ConnectionData> sendQueue = new LinkedList<>();

    public TCPMultiplexer(SocketChannel channel, Consumer<ConnectionData> listener) throws IOException {
        super(channel, SelectionKey.OP_READ);
        this.tcpConnection = new TCPConnection(channel);
        this.listener = listener;
    }

    public void sendToServer(ConnectionData connectionData) {
        CompletableFuture<ConnectionData> future = new CompletableFuture<>();
        synchronized (mutex) {
            sendQueue.push(connectionData);
            wakeUp();
        }
    }

    @Override
    public void run() {
        this.startProcessing();
    }

    @Override
    protected void onReadable(SelectionKey key) throws IOException {
        ConnectionData received = tcpConnection.receiveData();
        listener.accept(received);
    }

    @Override
    protected void onWritable(SelectionKey key) throws IOException {
        ConnectionData data = null;
        synchronized (mutex) {
            if (!sendQueue.isEmpty()) {
                data = sendQueue.pop();
            }
        }

        if (data != null) {
            tcpConnection.sendData(data);
            key.interestOps(SelectionKey.OP_READ);
        }
    }

    @Override
    protected void onEndConnection(SelectionKey key) throws IOException { this.stopProcessing(); }

    @Override
    protected void onWakeUp() {
        synchronized (mutex) {
            try {
                if (!sendQueue.isEmpty()) {
                    channel.register(selector, SelectionKey.OP_WRITE);
                }
            } catch (ClosedChannelException e) { this.stopProcessing(); }
        }
    }

    @Override
    protected void onTimeout() throws IOException { }    //never invoked

    @Override
    protected void onAcceptable(SelectionKey key) throws IOException { }    //never invoked
}
