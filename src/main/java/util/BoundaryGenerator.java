package util;

import java.util.Random;

public class BoundaryGenerator {

    private static final String PRE = "multipart/form-data;boundary=----WebKitFormBoundary";

    public static String getRandomBoundary() {
        int leftLimit = 48;
        int rightLimit = 122;
        int targetStringLength = 16;
        Random random = new Random();
        String generated = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        return PRE + generated;
    }
}
