package com.domenico.server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    @Test
    void equalsTest() {
        User user = new User("user", "fakePassw");
        User differentPassw = new User("user", "differentPass");
        User exactlySame = new User("user", "fakePassw");
        User different = new User("different", "something");
        assertEquals(user, exactlySame);
        assertNotEquals(user, different);
        assertNotEquals(user, differentPassw);
    }
}