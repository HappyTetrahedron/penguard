package verteiltesysteme.penguard.guardianservice;


public class JoinState {
    final static int STATE_JOINED = 1;
    final static int STATE_JOINED_FAILED = 2;
    final static int STATE_JOIN_INPROGRESS = 3;
    final static int STATE_NOT_JOINED = 4; //i.e. in a group with only oneself as a member
    int state = STATE_NOT_JOINED;
    
    String groupUN= ""; //the name of another group member of the group i'd like to join
    long timeStamp = 0;

    void reset(){
        state = STATE_NOT_JOINED;
        groupUN = "";
        timeStamp = 0;
    }

    void startGroupJoin(String groupUN){
        state = STATE_JOIN_INPROGRESS;
        this.groupUN = groupUN;
        timeStamp = System.currentTimeMillis();
    }

    void joinFailed(){
        reset();
    }

    void joinSuccessful(){
        state = STATE_JOINED;
    }

    //TODO add more if necessary
}
