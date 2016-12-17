package verteiltesysteme.penguard.guardianservice;

import verteiltesysteme.penguard.protobuf.PenguardProto;

public class CommitmentState {
    static final int STATE_IDLE = 1;
    static final int STATE_VOTED_YES = 2;
    static final int STATE_COMMIT_REQ_SENT = 4;
    int state = STATE_IDLE;

    /* Is called when 2pc aborts or commits */
    TwoPhaseCommitCallback commitCallback = null;

    PenguardProto.Group groupUpdate;
    PenguardProto.PGPGuardian initiant = null;

    boolean voteNoReceived = false;
    boolean voteYesReceived = false;

    void commitReqReceived(PenguardProto.PGPMessage grpChangeMessage, PenguardProto.PGPGuardian initiant) {
        this.initiant = initiant;

        groupUpdate = grpChangeMessage.getGroup();

        state = STATE_VOTED_YES;
    }

    void initiateCommit(PenguardProto.Group group, PenguardProto.PGPGuardian initiant, TwoPhaseCommitCallback callback) {
        groupUpdate = group;
        this.initiant = initiant;
        state = STATE_COMMIT_REQ_SENT;
        commitCallback = callback;
    }

    void abort() {
        if(commitCallback != null) { commitCallback.onAbort("2PC: abort"); }
        reset();
    }

    void commit() {
        if(commitCallback != null) { commitCallback.onCommit("2PC: commit"); }
        reset();
    }

    void reset() {
        initiant = null;
        groupUpdate = null;
        state = STATE_IDLE;
        voteNoReceived = false;
        voteYesReceived = false;
        commitCallback = null;
    }

    void voteNoReceived() {
        voteNoReceived = true;
    }

    void voteYesReceived() {
        voteYesReceived = true;
    }

}
