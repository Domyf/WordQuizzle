package com.domenico.server;

import com.domenico.communication.RMIConnection;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.UnicastRemoteObject;

/** This class implements the RMI connection between client and server. It is used to register a new user. */
public class UserRegistrationService extends RemoteServer implements RMIConnection {

    private UsersManagement usersManagement;

    public UserRegistrationService(UsersManagement usersManagement) {
        this.usersManagement = usersManagement;
    }

    @Override
    public void register(String username, String password) throws RemoteException {
        usersManagement.register(new User(username, password));
        System.out.println("Registration for the user "+username+" done");
    }

    public static UserRegistrationService newRegistrationService(UsersManagement usersManagement) throws RemoteException {
        UserRegistrationService userRegistrationService = new UserRegistrationService(usersManagement);
        RMIConnection stub = (RMIConnection) UnicastRemoteObject.exportObject(userRegistrationService, 0);

        LocateRegistry.createRegistry(RMIConnection.REGISTRY_PORT);
        Registry registry = LocateRegistry.getRegistry(RMIConnection.REGISTRY_PORT);

        registry.rebind(RMIConnection.SERVICE_NAME, stub);
        return userRegistrationService;
    }
}