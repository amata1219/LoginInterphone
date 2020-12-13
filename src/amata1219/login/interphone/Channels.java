package amata1219.login.interphone;

public class Channels {

    public static final String CHECK;
    public static final String RESULT;
    public static final String PLAY;

    static {
        String name = "LoginInterphone";
        String separator = ":";
        String prefix = name + separator;

        CHECK = prefix + "check";
        RESULT = prefix + "result";
        PLAY = prefix + "play";
    }


}
