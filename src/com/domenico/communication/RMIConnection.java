package com.domenico.communication;

import java.rmi.Remote;
import java.rmi.RemoteException;

/** Interface with all the Word Quizzle functionalities that should be done via RMI */
public interface RMIConnection extends Remote {

    String SERVICE_NAME = "REGISTER_USER_SERVICE";
    int REGISTRY_PORT = 9999;

    String register(String username, String password) throws RemoteException;
}
