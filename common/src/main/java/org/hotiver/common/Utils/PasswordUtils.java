package org.hotiver.common.Utils;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PasswordUtils {

    public static String generatePassword(int length) {
        final String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final String lower = "abcdefghijklmnopqrstuvwxyz";
        final String digits = "0123456789";
        final String special = "!@#$%^&*()-_=+[]{}";
        final String allChars = upper + lower + digits + special;

        SecureRandom random = new SecureRandom();
        List<Character> passwordChars = new ArrayList<>();

        passwordChars.add(upper.charAt(random.nextInt(upper.length())));
        passwordChars.add(lower.charAt(random.nextInt(lower.length())));
        passwordChars.add(digits.charAt(random.nextInt(digits.length())));
        passwordChars.add(special.charAt(random.nextInt(special.length())));

        for (int i = passwordChars.size(); i < 13; i++) {
            passwordChars.add(allChars.charAt(random.nextInt(allChars.length())));
        }

        Collections.shuffle(passwordChars, random);

        StringBuilder password = new StringBuilder();
        for (char c : passwordChars) {
            password.append(c);
        }

        return password.toString();
    }
}
