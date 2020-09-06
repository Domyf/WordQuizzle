package com.domenico.server;

/** Exception thrown when an error related to users management occurs */
public class UsersManagementException extends Exception {
    public UsersManagementException(String message) {
        super(message);
    }
}
