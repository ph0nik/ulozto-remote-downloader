package util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkValidator {


    private final static String LINK_VALIDATOR = "^https*:\\/\\/ulozto.net\\/file";

    public static boolean isProperUloztoLink(String pageLink) {
        Pattern p = Pattern.compile(LINK_VALIDATOR);
        Matcher matcher = p.matcher(pageLink);
        return matcher.find();
    }

    public static void main(String[] args) {
        String test = "http://ulozto.net/file/SUK8KnR43rqP/my-immortal-band-version-mp3#!ZJSuMwR2A2WvBGxjZwZ4Mwt0MQWwZyOkpwAkLzACpUEynTMuZD==";
        System.out.println(isProperUloztoLink(test));
    }


}
