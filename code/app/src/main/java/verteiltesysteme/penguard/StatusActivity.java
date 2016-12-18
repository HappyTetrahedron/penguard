package verteiltesysteme.penguard;

import android.os.Handler;
import android.os.Bundle;

public abstract class StatusActivity extends PenguardActivity {

    private static final int UPDATE_DELAY = 500;
    Handler handler;
    Runnable updateTask;
    boolean paused = false;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler();
        updateTask = new Runnable() {
            @Override
            public void run() {
                updateState();
                if (!paused) handler.postDelayed(this, UPDATE_DELAY);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        paused = false;
        handler.post(updateTask);
    }

    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        paused = false;
    }

    abstract void updateState();
}
