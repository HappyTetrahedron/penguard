package verteiltesysteme.penguard.guardianservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import verteiltesysteme.penguard.GPenguinDetailActivity;

/* Seems maybe slightly overkill to have an extra service for simply stopping an alarm via notification, so let me know if you have a better idea,
 * I couldn't find anything reasonable.
 */
public class StopAlarmService extends Service {
    private GuardianServiceConnection guardianServiceConnection;

    @Override
    public void onCreate(){
        guardianServiceConnection = new GuardianServiceConnection();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Get penguin address from calling intent.
        final String mac = intent.getStringExtra(GPenguinDetailActivity.EXTRA_PENGUIN_MAC);

        // Register a callback that stops the alarm and ends this service as soon as the binding stage of the guardianServiceConnection is done.
        guardianServiceConnection.registerServiceConnectedCallback(new Runnable() {
            @Override
            public void run() {
                guardianServiceConnection.stopAlarm(guardianServiceConnection.getPenguinById(mac));
                stopSelf();
            }
        });

        // Start binding to the service.
        Intent bindServiceIntent = new Intent(this, GuardService.class);
        bindService(bindServiceIntent, guardianServiceConnection, BIND_AUTO_CREATE);
        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        unbindService(guardianServiceConnection);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void debug(String msg) {
        Log.d("StopAlarmService", msg);
    }
}
