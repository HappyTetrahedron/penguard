package verteiltesysteme.penguard;

import verteiltesysteme.penguard.guardianservice.GuardServiceError;

public interface GLoginCallback {
    void registrationSuccess();

    void registrationFailure(GuardServiceError errorType);
}
