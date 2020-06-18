package com.domenico.server;

import com.domenico.communication.UDPConnection;

import java.io.IOException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.Iterator;
import java.util.Set;

public abstract class Multiplexer extends Thread {

    protected SelectableChannel channel;
    protected Selector selector;

    public Multiplexer(SelectableChannel channel, int firstOp) throws IOException {
        this.channel = channel;
        channel.configureBlocking(false);
        selector = Selector.open();
        channel.register(selector, firstOp);
    }

    public Multiplexer(AbstractSelectableChannel channel, int op, Object attachment) throws IOException {
        this(channel, op);
        channel.register(selector, op, attachment);
    }

    @Override
    public void run() {
        try {
            while (true) {
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    try {
                        if (!key.isValid())
                            continue;
                        if (key.isAcceptable()) {
                            onClientAcceptable(key);
                        } else if (key.isReadable()) {
                            onClientReadable(key);
                        } else if (key.isWritable()) {
                            onClientWritable(key);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        key.cancel();
                        key.channel().close();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called when the method accept() will not block the thread
     */
    abstract void onClientAcceptable(SelectionKey key) throws IOException;

    /**
     * Called when the method read() will not block the thread
     */
    abstract void onClientReadable(SelectionKey key) throws IOException;

    /**
     * Called when the method write() will not block the thread
     */
    abstract void onClientWritable(SelectionKey key) throws IOException;
}