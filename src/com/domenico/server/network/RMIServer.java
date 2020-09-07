package com.domenico.server.network;

import com.domenico.communication.RMIConnection;
import com.domenico.server.usersmanagement.UsersManagement;
import com.domenico.server.usersmanagement.UsersManagementException;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.UnicastRemoteObject;

/** This class implements the RMI connection between client and server. It is used to register a new user. */
public class RMIServer extends RemoteServer implements RMIConnection {

    private final UsersManagement usersManagement;

    private RMIServer() throws IOException {
        this.usersManagement = UsersManagement.getInstance();
    }

    @Override
    synchronized public String register(String username, String password) throws RemoteException {
        try {
            usersManagement.register(username, password);
        } catch (UsersManagementException e) {  //if it's not possible to register the user
            return e.getMessage();
        }
        return "";
    }

    public static void newRegistrationService() throws IOException {
        RMIServer RMIServer = new RMIServer();
        RMIConnection stub = (RMIConnection) UnicastRemoteObject.exportObject(RMIServer, 0);

        LocateRegistry.createRegistry(RMIConnection.REGISTRY_PORT);
        Registry registry = LocateRegistry.getRegistry(RMIConnection.REGISTRY_PORT);

        registry.rebind(RMIConnection.SERVICE_NAME, stub);
    }
}