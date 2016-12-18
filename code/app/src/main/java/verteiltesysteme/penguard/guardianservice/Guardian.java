package verteiltesysteme.penguard.guardianservice;


import android.util.Log;

import java.util.Objects;

import verteiltesysteme.penguard.protobuf.PenguardProto;

public class Guardian {

    private String ip;

    private boolean hasBadNat = false;

    private int port;

    private String name;

    private long lastSeenTimestamp;

    // Amount of seconds after which guardian is assumed as missing. This influences the penguin alarm behaviour.
    private final double GUARDIAN_MISSING_THRESHOLD = 20;

    Guardian() {
        name = null;
        ip = null;
        port = 0;
    }

    Guardian(String name, String ip, int port) {
        this.name = name;
        this.ip = ip;
        this.port = port;
    }

    boolean hasBadNat() {
        return hasBadNat;
    }

    void setBadNat(boolean badNat) {
        hasBadNat = badNat;
    }

    PenguardProto.PGPGuardian toProto() {
        PenguardProto.PGPGuardian pgpguardian = PenguardProto.PGPGuardian.newBuilder()
                .setName(getName())
                .setIp(getIp())
                .setPort(getPort())
                .setBadNat(hasBadNat())
                .build();
        return pgpguardian;
    }
    void updateTime() {
        lastSeenTimestamp = System.currentTimeMillis();
    }

    void setIp(String ip) {
        this.ip = ip;
    }

    public long getTimeStamp(){
        return  this.lastSeenTimestamp;
    }

    public boolean equals(Guardian guardian){
        return Objects.equals(this.getName(), guardian.getName());
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    void setPort(int port) {
        this.port = port;
    }

    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    boolean isGuardianMissing(){
        return (System.currentTimeMillis() - lastSeenTimestamp) / 1000.0 > GUARDIAN_MISSING_THRESHOLD;
    }

    private void debug(String msg) {
        Log.d("Guardian", msg);
    }
}
