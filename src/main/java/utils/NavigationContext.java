package utils;

public class NavigationContext {
    private static DashboardView targetView = DashboardView.HOME;

    public static void setTargetView(DashboardView view) {
        targetView = view;
    }

    public static DashboardView consumeTargetView() {
        DashboardView view = targetView;
        targetView = DashboardView.HOME; // reset nakon čitanja
        return view;
    }
}
