package com.domenico.server;

import com.domenico.communication.RMIConnection;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.UnicastRemoteObject;

/** This class implements the RMI connection between client and server. It is used to register a new user. */
public class UserRegistrationService extends RemoteServer implements RMIConnection {

    private UsersManagement usersManagement;

    public UserRegistrationService() throws IOException {
        this.usersManagement = UsersManagement.getInstance();
    }

    @Override
    public String register(String username, String password) throws RemoteException {
        try {
            usersManagement.register(username, password);
        } catch (UsersManagementException e) {
            return e.getMessage();
        }
        return "";
    }

    public static UserRegistrationService newRegistrationService() throws IOException {
        UserRegistrationService userRegistrationService = new UserRegistrationService();
        RMIConnection stub = (RMIConnection) UnicastRemoteObject.exportObject(userRegistrationService, 0);

        LocateRegistry.createRegistry(RMIConnection.REGISTRY_PORT);
        Registry registry = LocateRegistry.getRegistry(RMIConnection.REGISTRY_PORT);

        registry.rebind(RMIConnection.SERVICE_NAME, stub);
        return userRegistrationService;
    }
}