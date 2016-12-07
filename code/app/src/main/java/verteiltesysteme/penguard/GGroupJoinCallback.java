package verteiltesysteme.penguard;


public interface GGroupJoinCallback {
    void joinSuccessful();

    void joinFailure(String error);

}
