package utils;

public class NavigationContext {
    private static DashboardView targetView = DashboardView.THESIS;

    public static void setTargetView(DashboardView view) {
        targetView = view;
    }

    public static DashboardView consumeTargetView() {
        DashboardView view = targetView;
        targetView = DashboardView.THESIS; // reset nakon čitanja
        return view;
    }
}
