package Utils;

public class EmailUtils {

    public static boolean isValidEmail(String email) {
        String regex = "^[\\w.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(regex);
    }
}
