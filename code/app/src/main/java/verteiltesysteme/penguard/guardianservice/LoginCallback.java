package verteiltesysteme.penguard.guardianservice;

public interface LoginCallback {
    void registrationSuccess(String uuid);

    void registrationFailure(String error);
}
