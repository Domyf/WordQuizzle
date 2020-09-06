package com.domenico.client;

import com.domenico.communication.ConnectionData;
import com.domenico.communication.TCPConnection;
import com.domenico.shared.Multiplexer;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.function.Consumer;

/** This class extends the {@link Multiplexer} class and it handles all the TCP communications from and to the
 * WordQuizzle server. When a message arrives, it calls the message handler which will
 * think about handling the message while this class can continue its work. It is possible to send a message to the
 * server. On that case, the messages that should be sent are put in a queue and they will be sent to the server in the
 * same order the client asked to this class to send the messages. */
public class TCPMultiplexer extends Multiplexer implements Runnable {

    //Utility object that wraps all the work that should be done to send or receive a ConnectionData object
    private final TCPConnection tcpConnection;
    //handler method that is called when a message from the server arrives
    private final Consumer<ConnectionData> handler;
    private final Object mutex = new Object();  //mutex to protect the queue
    //queue with all the messages that should be sent to the server via TCP
    private final LinkedList<ConnectionData> sendQueue = new LinkedList<>();

    public TCPMultiplexer(SocketChannel channel, Consumer<ConnectionData> handler) throws IOException {
        super(channel, SelectionKey.OP_READ);
        this.tcpConnection = new TCPConnection(channel);
        this.handler = handler;
    }

    /** Sends the given message to the server */
    public void sendToServer(ConnectionData connectionData) {
        synchronized (mutex) {
            sendQueue.push(connectionData);
            wakeUp();
        }
    }

    @Override
    public void run() { this.startProcessing(); }

    @Override
    protected void onReadable(SelectionKey key) throws IOException {
        ConnectionData received = tcpConnection.receiveData();
        handler.accept(received);
    }

    @Override
    protected void onWritable(SelectionKey key) throws IOException {
        ConnectionData data = null;
        synchronized (mutex) {
            if (!sendQueue.isEmpty()) {
                data = sendQueue.pop(); //get the next message that should be sent
            }
        }

        if (data != null) {
            tcpConnection.sendData(data);   //send the data
            key.interestOps(SelectionKey.OP_READ);  //put the interest on reading the response
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
