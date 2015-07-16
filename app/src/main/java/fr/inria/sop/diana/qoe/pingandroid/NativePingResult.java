package fr.inria.sop.diana.qoe.pingandroid;

import java.net.InetAddress;

/**
 * Created by User on 16/07/2015.
 */
public class NativePingResult extends PingResult {
    private String _rawOutput;

    public NativePingResult(int packetsSent, int packetsLost, float minRtt, float avgRtt, float maxRtt, float rttStdDeviation, InetAddress targetAddress, String rawOutput) {
        super(packetsSent, packetsLost, minRtt, avgRtt, maxRtt, rttStdDeviation, targetAddress);
        _rawOutput = rawOutput;
    }

    public String getRawOutput() {
        return _rawOutput;
    }
}
