package com.domenico.client;

import com.domenico.communication.ConnectionData;

public interface TCPListener {
    void onTCPDataReceived(ConnectionData data);
}
