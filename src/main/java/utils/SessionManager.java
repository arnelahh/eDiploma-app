package utils;

import java.util.Timer;
import java.util.TimerTask;

public class SessionManager {
    private static Timer timer;
    private static final long SESSION_TIMEOUT = 2 * 60 * 60 * 1000;

    private static Runnable onSessionExpiredCallback;

    public static void startSession(Runnable onSessionExpired) {
        onSessionExpiredCallback = onSessionExpired;
        resetTimer();
    }

    public static void resetTimer() {
        if(timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(onSessionExpiredCallback != null) {
                    onSessionExpiredCallback.run();
                }
            }
        }, SESSION_TIMEOUT);
    }
}
