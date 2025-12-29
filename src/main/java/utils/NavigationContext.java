package utils;

import model.AppUser;

public class NavigationContext {
    private static DashboardView targetView = DashboardView.THESIS;

    public static void setTargetView(DashboardView view) {
        targetView = view;
    }
    private static AppUser currentUser;

    public static DashboardView consumeTargetView() {
        DashboardView view = targetView;
        targetView = DashboardView.THESIS; // reset nakon čitanja
        return view;
    }

    public static void setCurrentUser(AppUser user) {
        currentUser = user;
    }

    public static AppUser getCurrentUser() {
        return currentUser;
    }
}
