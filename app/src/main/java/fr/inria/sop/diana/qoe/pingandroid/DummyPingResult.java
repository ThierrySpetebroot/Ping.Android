package fr.inria.sop.diana.qoe.pingandroid;

import java.net.InetAddress;

/**
 * Created by User on 13/07/2015.
 */
public class DummyPingResult implements IPingResult {
    private String string;

    public DummyPingResult(String s) {
        string = s;
    }

    public String getRawOutput() {
        return string;
    }

    @Override
    public int getPacketsLost() {
        return 0;
    }

    @Override
    public int getPacketSent() {
        return 0;
    }

    @Override
    public float getMinRtt() {
        return 0;
    }

    @Override
    public float getAvgRtt() {
        return 0;
    }

    @Override
    public float getMaxRtt() {
        return 0;
    }

    @Override
    public float getRttMeanDeviation() {
        return 0;
    }

    @Override
    public InetAddress getTargetAddress() {
        return null;
    }
}
