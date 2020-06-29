package com.domenico.server;

import java.util.*;

public class UsersManagement {

    private static UsersManagement instance;
    private HashMap<User, List<String>> users;  //map user -> its friend's usernames
    private Leaderboard leaderboard;

    private UsersManagement() {
        this.users = new HashMap<>();
        this.leaderboard = new Leaderboard();
    }

    public static UsersManagement getInstance() {
        if (instance == null)
            instance = new UsersManagement();
        return instance;
    }

    public void register(User user) throws UsersManagementException {
        if (findByUsername(user.getUsername()) != null)
            throw new UsersManagementException("Non è possibile registrarsi: questo nome utente è già utilizzato");

        users.put(user, new ArrayList<>());
    }

    public void login(User user) throws UsersManagementException {
        User found = find(user);
        if (found == null)
            throw new UsersManagementException("Username o password non valida");
        if (found.isLoggedIn())
            throw new UsersManagementException("Hai già eseguito il login");

        found.setLoggedIn(true);
    }

    public void logout(String username) throws UsersManagementException {
        User found = findByUsername(username);
        if (found == null)
            throw new UsersManagementException("Username o password non valida");
        if (!found.isLoggedIn())
            throw new UsersManagementException("Hai già eseguito il logout");

        found.setLoggedIn(false);
    }

    public void addFriend(String username, String friendUsername) throws UsersManagementException {
        if (username.equals(friendUsername))
            throw new UsersManagementException("Non puoi diventare amico con te stesso");
        User user = findByUsername(username);
        User friendUser = findByUsername(friendUsername);
        if (user == null)
            throw new UsersManagementException("Username non valida");
        if (friendUser == null)
            throw new UsersManagementException("Non esiste un altro utente con questa username");

        List<String> friends = users.get(user);
        List<String> otherFriends = users.get(friendUser);
        if (areFriends(user, friendUser)) {
            throw new UsersManagementException("L'amicizia non è stata creata perchè siete già amici");
        } else {
            friends.add(friendUsername);
            otherFriends.add(username);
        }
    }

    public List<String> getFriendList(String username) throws UsersManagementException {
        User found = findByUsername(username);
        if (found == null) {
            throw new UsersManagementException("Invalid username");
        }
        // TODO: 26/06/2020 return a JSON object
        return users.get(found);
    }

    public int getScore(String username) {
        return leaderboard.getScore(username);
    }

    public boolean areFriends(String first, String second) {
        User firstUser = findByUsername(first);
        if (firstUser == null)
            return false;
        User secondUser = findByUsername(second);
        if (secondUser == null)
            return false;

        return areFriends(firstUser, secondUser);
    }

    public boolean isOnline(String username) {
        User user = findByUsername(username);
        return user != null && user.isLoggedIn();
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
        List<String> firstFriends = users.get(first);
        return firstFriends.contains(second.getUsername());
    }
}
