package verteiltesysteme.penguard.guardianservice;

import android.util.Log;

import java.util.UUID;

import verteiltesysteme.penguard.GLoginCallback;

class RegistrationState {

    final static int STATE_UNREGISTERED = 1;
    final static int STATE_REGISTRATION_IN_PROGRESS = 3;
    final static int STATE_REGISTERED = 2;
    int state = STATE_UNREGISTERED;
    String username = "";
    UUID uuid = null;
    GLoginCallback loginCallback = null;

    void reset() {
        state = STATE_UNREGISTERED;
        username = "";
        uuid = null;
        loginCallback = null;
    }

    void registrationProcessStarted(String username, GLoginCallback callback) {
        this.username = username;
        this.loginCallback = callback;
        state = STATE_REGISTRATION_IN_PROGRESS;
    }

    void registrationFailed(String error) {
        if (loginCallback != null) loginCallback.registrationFailure(error);
        reset();
    }

    void registrationSucceeded(UUID uuid) {
        if (loginCallback != null) loginCallback.registrationSuccess();
        this.uuid = uuid;
        state = STATE_REGISTERED;
    }

    private void debug(String msg) {
        Log.d("RegistrationState", msg);
    }

}
