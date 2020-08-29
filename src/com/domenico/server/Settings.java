package com.domenico.server;

public class Settings {

    public static final String ITALIAN_WORDS_FILENAME = "1000_common_italian_words.txt";
    public static final int MAX_WAITING_TIME = 5000;   //how much time the user has to accept or decline a challenge
    public static final int CHALLENGE_WORDS = 8;        //how many words the users will translate during a challenge
    public final static int POINTS_RIGHT_TRANSLATION = 1;   //how many points the user get when the translation is right
    public final static int POINTS_ERROR_PENALTY = 1;   //what's the penalty, in terms of user's points, when the translation is wrong
    public final static int POINTS_EXTRA_POINTS = 10;   //how many points the user gets when it wins the challenge

}
