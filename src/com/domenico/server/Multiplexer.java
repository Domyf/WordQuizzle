package com.domenico.server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Abstract class that implements the multiplexing logic of a channel and it extends the Thread class.
 * When the accept() method call will not block on a channel, the method onAcceptable is called and the relative
 * selection key is passed. When the read() method call will not block on a channel, the method onReadable is called
 * and the relative selection key is passed. When the write() method call will not block on a channel, the method
 * onWritable is called and the relative selection key is passed.
 */
public abstract class Multiplexer {

    protected AbstractSelectableChannel channel;
    protected Selector selector;

    private boolean running;
    private int timeout;

    /**
     * Instantiates this with the given channel. The first operation registered is that one given as argument. It is
     * the main constructor.
     * @param channel the channel on which the multiplexing is done
     * @param firstOp the first operation on which the channel should be registered
     * @throws IOException if an I/O error occurs
     */
    public Multiplexer(AbstractSelectableChannel channel, int firstOp) throws IOException {
        this.channel = channel;
        this.timeout = 0;
        this.selector = Selector.open();
        this.channel.configureBlocking(false);
        this.channel.register(selector, firstOp);
    }

    /**
     * A secondary constructor that calls the main constructor and takes the timeout value as well
     * @param channel the channel on which the multiplexing is done
     * @param firstOp the first operation on which the channel should be registered
     * @param timeout the timeout done for the selection. Zero for infinite timeout. Must be non negative
     * @throws IOException if an I/O error occurs
     */
    public Multiplexer(AbstractSelectableChannel channel, int firstOp, int timeout) throws IOException {
        this(channel, firstOp);
        this.timeout = timeout;
    }

    public void startProcessing() {
        running = true;
        try {
            while (running) {
                if (selector.select(timeout) == 0) {
                    onTimeout();
                    continue;
                }
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();  //removes this key
                    try {
                        if (!key.isValid())
                            continue;
                        if (key.isAcceptable()) {
                            onAcceptable(key);  //The accept() method will not block
                        } else if (key.isReadable()) {
                            onReadable(key);    //The read() method will not block
                        } else if (key.isWritable()) {
                            onWritable(key);    //The write() method will not block
                        }
                    } catch (IOException e) {
                        key.cancel();   //This endpoint will not be taken in account in the next selections
                        if (key.channel().isOpen())
                            onEndConnection(key);   //The connection with the endpoint should be ended
                    }
                }
            }
        } catch (IOException e) {
            stopProcessing();
        }
    }

    protected void stopProcessing() {
        running = false;
    }

    protected abstract void onTimeout() throws IOException;

    /**
     * Called when the method accept() will not block the thread.
     * @param key the selection key relative to that channel on which the accept() method will not block the thread
     * @throws IOException if an I/O error occurs
     */
    abstract void onAcceptable(SelectionKey key) throws IOException;

    /**
     * Called when the method read() will not block the thread
     * @param key the selection key relative to that channel on which the read() method will not block the thread
     * @throws IOException if an I/O error occurs
     */
    abstract void onReadable(SelectionKey key) throws IOException;

    /**
     * Called when the method write() will not block the thread
     * @param key the selection key relative to that channel on which the write() method will not block the thread
     * @throws IOException if an I/O error occurs
     */
    abstract void onWritable(SelectionKey key) throws IOException;

    /**
     * Called when it is needed to close the connection with the endpoint
     * @param key the selection key relative to that channel that should be closed
     * @throws IOException if an I/O error occurs
     */
    abstract void onEndConnection(SelectionKey key) throws IOException;
}