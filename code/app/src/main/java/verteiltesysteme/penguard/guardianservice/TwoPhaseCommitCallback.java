package verteiltesysteme.penguard.guardianservice;


public interface TwoPhaseCommitCallback {
    void onCommit(String message);

    void onAbort(String error);

}
