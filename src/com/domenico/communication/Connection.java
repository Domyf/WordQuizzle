package com.domenico.communication;

import java.io.IOException;
import java.nio.channels.spi.AbstractSelectableChannel;

public interface Connection {
    void sendRequest(Request request) throws IOException;
    void sendResponse(Response response) throws IOException;
    Response getResponse() throws IOException;
    Request getRequest() throws IOException;
    void endConnection() throws IOException;
}
