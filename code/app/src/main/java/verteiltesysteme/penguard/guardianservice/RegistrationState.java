package verteiltesysteme.penguard.guardianservice;

import android.util.Log;

import java.util.UUID;

class RegistrationState {

    final static int STATE_UNREGISTERED = 1;
    final static int STATE_REGISTRATION_IN_PROGRESS = 3;
    final static int STATE_REGISTERED = 2;
    int state = STATE_UNREGISTERED;
    String username = "";
    UUID uuid = null;
    LoginCallback loginCallback = null;

    void reset() {
        state = STATE_UNREGISTERED;
        username = "";
        uuid = null;
        loginCallback = null;
    }

    void registrationProcessStarted(String username, LoginCallback callback) {
        this.username = username;
        this.loginCallback = callback;
        state = STATE_REGISTRATION_IN_PROGRESS;
    }

    void registrationFailed(String error) {
        if (loginCallback != null) loginCallback.registrationFailure(error);
        reset();
    }

    void registrationSucceeded(UUID uuid) {
        if (loginCallback != null) loginCallback.registrationSuccess(uuid.toString());
        this.uuid = uuid;
        state = STATE_REGISTERED;
    }

    private void debug(String msg) {
        Log.d("RegistrationState", msg);
    }

}
