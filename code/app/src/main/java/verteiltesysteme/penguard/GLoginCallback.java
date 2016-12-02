package verteiltesysteme.penguard;

public interface GLoginCallback {
    void registrationSuccess();

    void registrationFailure(String error);
}
