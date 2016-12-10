package verteiltesysteme.penguard.guardianservice;


public interface GroupJoinCallback {
    void joinSuccessful();

    void joinAccepted();

    void joinFailure(String error);

}
