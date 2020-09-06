package com.domenico.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** Class that contains a method to load the settings from the server resources and all the settings stored as public
 * static constants.
 */
public class Settings {

    //the italian words file name
    private static String italianWordsFilename;
    //how much time the user has to accept or decline a challenge
    private static int challengeRequestTimeout;
    //how many milliseconds is the challenge long
    private static long maxChallengeLength;
    //how many words the users will translate during a challenge
    private static int challengeWords;
    //how many points the user get when the translation is right
    private static int pointsRightTranslation;
    //what's the penalty, in terms of user's points, when the translation is wrong
    private static int pointsErrorPenalty;
    //how many points the user gets when it wins the challenge
    private static int extraPoints;

    public static void loadSettings(InputStream inputStream) throws IOException {
        Properties prop = new Properties();
        prop.load(inputStream);
        italianWordsFilename = prop.getProperty("italian_words_filename");
        challengeRequestTimeout = Integer.parseUnsignedInt((String) prop.get("challenge_request_timeout"));
        maxChallengeLength = Integer.parseUnsignedInt((String) prop.get("max_challenge_length"));
        challengeWords = Integer.parseUnsignedInt((String) prop.get("challenge_words"));
        pointsRightTranslation = Integer.parseUnsignedInt((String) prop.get("points_right_translation"));
        pointsErrorPenalty = Integer.parseUnsignedInt((String) prop.get("points_error_penalty"));
        extraPoints = Integer.parseUnsignedInt((String) prop.get("extra_points"));
    }

    public static String getItalianWordsFilename() {
        return italianWordsFilename;
    }

    public static int getChallengeRequestTimeout() {
        return challengeRequestTimeout;
    }

    public static long getMaxChallengeLength() {
        return maxChallengeLength;
    }

    public static int getChallengeWords() {
        return challengeWords;
    }

    public static int getPointsRightTranslation() {
        return pointsRightTranslation;
    }

    public static int getPointsErrorPenalty() {
        return pointsErrorPenalty;
    }

    public static int getExtraPoints() {
        return extraPoints;
    }
}
