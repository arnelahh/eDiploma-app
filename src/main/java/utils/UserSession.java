package utils;

import model.AppUser;

public class UserSession {
    private static AppUser loggedUser;

    public static void setUser(AppUser user) {
        loggedUser = user;
    }

    public static AppUser getUser() {
        return loggedUser;
    }

    public static void clear() {
        loggedUser = null;
    }
}
