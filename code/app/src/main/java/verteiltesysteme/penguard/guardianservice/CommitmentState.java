package verteiltesysteme.penguard.guardianservice;

import verteiltesysteme.penguard.protobuf.PenguardProto;

public class CommitmentState {
    static final int STATE_IDLE = 1;
    static final int STATE_VOTED_YES = 2;
    static final int STATE_VOTED_NO = 3;
    static final int STATE_COMMIT_REQ_SENT = 4;

    PenguardProto.PGPMessage groupUpdate;
    String initiantName;
}
