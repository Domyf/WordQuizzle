package com.domenico.server;

import com.domenico.server.usersmanagement.UserData;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

class UserDataTest {

    private static final String JSON_RESULT = "{\"score\":150,\"password\":\"username\",\"friends\":[\"friend1\",\"friend2\"]}";
    private static final String USERNAME_TEST = "username";
    private static final String FRIEND1_TEST = "friend1";
    private static final String FRIEND2_TEST = "friend2";
    private static final int SCORE_TEST = 150;
    private UserData data;

    @BeforeEach
    void setUp() {
        data = new UserData(USERNAME_TEST);
        data.addScore(SCORE_TEST);
        data.addFriend(FRIEND1_TEST);
        data.addFriend(FRIEND2_TEST);
    }

    @Test
    void newFromJSON() throws IOException {
        //Writes userdata into JSON
        StringWriter writer = new StringWriter();
        data.writeJSONString(writer);
        //Reads from JSON into JSONObject
        StringReader reader = new StringReader(writer.toString());
        JSONObject parsed = (JSONObject) JSONValue.parse(reader);
        //Creates a new UserData based on the JSONObject read
        UserData read = UserData.newFromJSON(parsed);
        //Check if it has same score, same password, same friends
        assertEquals(this.data.getScore(), read.getScore());
        assertEquals(this.data.getPassword(), read.getPassword());
        for (String friend :this.data.getFriends()) {
            assertTrue(read.hasFriend(friend));
        }
        assertEquals(this.data.getFriends().size(), read.getFriends().size());
    }

    @Test
    void writeJSONString() throws IOException {
        StringWriter writer = new StringWriter();
        data.writeJSONString(writer);
        assertEquals(writer.toString(), JSON_RESULT);
    }

    @Test
    void addFriend() {
        data.addFriend("test");
        assertTrue(data.hasFriend("test"));
    }
}