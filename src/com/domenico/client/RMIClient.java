package com.domenico.client;

import com.domenico.communication.RMIConnection;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIClient {

    private RMIConnection rmiConnection;

    public RMIClient() throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry(RMIConnection.REGISTRY_PORT);
        Remote remoteObj = registry.lookup(RMIConnection.SERVICE_NAME);
        this.rmiConnection = (RMIConnection) remoteObj;
    }

    public String register(String username, String password) throws RemoteException {
        return rmiConnection.register(username, password);
    }
}
