package com.domenico.server;

import java.util.*;

public class UsersManagementImpl implements UsersManagement {

    private static UsersManagementImpl instance;
    private HashMap<User, List<String>> users;  //map user -> its friend's usernames

    private UsersManagementImpl() {
        this.users = new HashMap<>();
    }

    public static UsersManagementImpl getInstance() {
        if (instance == null)
            instance = new UsersManagementImpl();
        return instance;
    }

    @Override
    public void register(User user) throws IllegalArgumentException {
        if (findByUsername(user.getUsername()) != null)
            throw new IllegalArgumentException("Non è possibile registrarsi: questo nome utente è già utilizzato");

        users.put(user, new ArrayList<>());
    }

    @Override
    public void login(User user) throws IllegalArgumentException {
        User found = find(user);
        if (found == null)
            throw new IllegalArgumentException("Username o password non valida");
        if (found.isLoggedIn())
            throw new IllegalArgumentException("Hai già eseguito il login");

        found.setLoggedIn(true);
    }

    @Override
    public void logout(String username) throws IllegalArgumentException {
        User found = findByUsername(username);
        if (found == null)
            throw new IllegalArgumentException("Username o password non valida");
        if (!found.isLoggedIn())
            throw new IllegalArgumentException("Hai già eseguito il logout");

        found.setLoggedIn(false);
    }

    @Override
    public void addFriend(String username, String friendUsername) throws IllegalArgumentException {
        User user = findByUsername(username);
        User friendUser = findByUsername(friendUsername);
        if (user == null)
            throw new IllegalArgumentException("Username non valida");
        if (friendUser == null)
            throw new IllegalArgumentException("Non esiste un altro utente con questa username");

        List<String> friends = users.get(user);
        List<String> otherFriends = users.get(friendUser);
        if (areFriends(user, friendUser)) {
            throw new IllegalArgumentException("L'amicizia non è stata creata perchè siete già amici");
        } else {
            friends.add(friendUsername);
            otherFriends.add(username);
        }
    }

    @Override
    public List<String> getFriendList(String username) throws IllegalArgumentException {
        User found = findByUsername(username);
        if (found == null) {
            throw new IllegalArgumentException("Invalid username");
        }
        // TODO: 26/06/2020 return a JSON object
        return users.get(found);
    }

    private User find(User user) {
        for (User registeredUser:users.keySet()) {
            if (registeredUser.equals(user))
                return registeredUser;
        }

        return null;
    }

    private User findByUsername(String username) {
        for (User user :users.keySet()) {
            if (user.getUsername().equals(username))
                return user;
        }
        return null;
    }

    private boolean areFriends(User first, User second) {
        List firstFriends = users.get(first);
        return firstFriends.contains(second.getUsername());
    }
}
