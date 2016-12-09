package verteiltesysteme.penguard.guardianservice;


import verteiltesysteme.penguard.GGroupJoinCallback;

public class JoinState {
    final static int STATE_IDLE = 1;
    final static int STATE_JOIN_REQ_SENT = 2;
    final static int STATE_JOIN_INPROGRESS = 3;
    int state = STATE_IDLE;

    private GGroupJoinCallback callback = null;
    
    String groupUN= ""; //the name of another group member of the group i'd like to join
    long timeStamp = 0;

    void reset(){
        state = STATE_IDLE;
        groupUN = "";
        timeStamp = 0;
    }

    void startGroupJoin(String groupUN, GGroupJoinCallback callback){
        state = STATE_JOIN_REQ_SENT;
        this.groupUN = groupUN;
        this.callback = callback;
        timeStamp = System.currentTimeMillis();
    }

    void joinFailed(String error){
        if (callback != null) callback.joinFailure(error);
        reset();
    }

    void joinReqAccepted() {
        if (callback != null) callback.joinAccepted();
        state = STATE_JOIN_INPROGRESS;
    }

    void joinSuccessful(){
        if (callback != null) callback.joinSuccessful();
        reset();
    }

}
