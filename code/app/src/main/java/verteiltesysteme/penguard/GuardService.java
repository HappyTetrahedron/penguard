package verteiltesysteme.penguard;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class GuardService extends Service {

    IBinder binder = new PenguinGuardBinder();

    public GuardService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public class PenguinGuardBinder extends Binder{
        GuardService getService(){
            return GuardService.this;
        }
    }
}
