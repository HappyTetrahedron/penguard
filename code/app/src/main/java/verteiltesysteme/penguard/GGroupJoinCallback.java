package verteiltesysteme.penguard;


public interface GGroupJoinCallback {
    void joinSuccessful();

    void joinAccepted();

    void joinFailure(String error);

}
