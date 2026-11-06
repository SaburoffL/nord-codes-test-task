package com.nordcodes.utils;

import java.util.Random;
import java.util.UUID;

public class TokenHelper {

    public static String generate() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .toUpperCase();
    }

    //31 символ
    public static String generateShortToken() {
        return generate().substring(1);
    }

    //33 символа
    public static String generateLongToken() {
        return generate() + "A";
    }

    //строчные буквы
    public static String generateLowercaseToken() {
        return generate().toLowerCase();
    }

    private static final String SPECIAL_CHARACTERS = "!@#$%^&*()-_=+";
    private static final Random RANDOM = new Random();


    //спецсимволы
    public static String generateTokenWithSpecialCharacters() {
        return generate().substring(0, 31) + SPECIAL_CHARACTERS.charAt(RANDOM.nextInt(SPECIAL_CHARACTERS.length()));
    }

}
