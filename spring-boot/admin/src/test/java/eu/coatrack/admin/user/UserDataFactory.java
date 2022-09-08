package eu.coatrack.admin.user;

import eu.coatrack.api.User;

public class UserDataFactory {

    public final static String emailVerificationCode = "abc";
    public final static User user = getUser(1L, "Pete", emailVerificationCode);

    public static User getUser(long id, String name, String emailVerificationCode) {
        User newUser = new User();
        newUser.setId(id);
        newUser.setUsername(name);
        newUser.setEmailVerifiedUrl(emailVerificationCode);
        return newUser;
    }
}
