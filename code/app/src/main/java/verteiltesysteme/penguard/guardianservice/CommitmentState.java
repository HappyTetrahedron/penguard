package verteiltesysteme.penguard.guardianservice;

import verteiltesysteme.penguard.protobuf.PenguardProto;

public class CommitmentState {
    static final int STATE_IDLE = 1;
    static final int STATE_VOTED_YES = 2;
    static final int STATE_COMMIT_REQ_SENT = 4;
    int state = STATE_IDLE;

    PenguardProto.Group groupUpdate;
    String initiantName;
    String initiantHost;
    int initiantPort;

    boolean voteNoReceived = false;
    boolean voteYesReceived = false;

    void commitReqReceived(PenguardProto.PGPMessage grpChangeMessage, String host, int port) {
        initiantHost = host;
        initiantPort = port;
        initiantName = grpChangeMessage.getName();

        groupUpdate = grpChangeMessage.getGroup();

        state = STATE_VOTED_YES;
    }

    void initiateCommit(PenguardProto.Group group, Guardian initiant) {
        groupUpdate = group;
        initiantName = initiant.getName();
        state = STATE_COMMIT_REQ_SENT;
    }

    void abort() {
        reset();
    }

    void commit() {
        reset();
    }

    void reset() {
        initiantHost = "";
        initiantPort = 0;
        initiantName = "";
        groupUpdate = null;
        state = STATE_IDLE;
        voteNoReceived = false;
        voteYesReceived = false;
    }

    void voteNoReceived() {
        voteNoReceived = true;
    }

    void voteYesReceived() {
        voteYesReceived = true;
    }

}
