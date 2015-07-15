package fr.inria.sop.diana.qoe.pingandroid;

import java.net.InetAddress;

/**
 * Created by User on 14/07/2015.
 */
public class PingResult implements IPingResult {
    private int _packetsLost;
    private int _packetsSent;

    private float _minRtt;
    private float _avgRtt;
    private float _maxRtt;
    private float _rttStdDeviation;

    private InetAddress _targetAddress;

    public PingResult(int packetsSent, int packetsLost, float minRtt, float avgRtt, float maxRtt, float rttStdDeviation, InetAddress targetAddress) {
        _packetsLost = packetsLost;
        _packetsSent = packetsSent;

        _minRtt = minRtt;
        _avgRtt = avgRtt;
        _maxRtt = maxRtt;
        _rttStdDeviation = rttStdDeviation;

        _targetAddress = targetAddress;
    }

    @Override
    public int getPacketsLost() {
        return _packetsLost;
    }

    @Override
    public int getPacketSent() {
        return _packetsSent;
    }

    @Override
    public float getMinRtt() {
        return _minRtt;
    }

    @Override
    public float getAvgRtt() {
        return _avgRtt;
    }

    @Override
    public float getMaxRtt() {
        return _maxRtt;
    }

    @Override
    public float getRttMeanDeviation() {
        return _rttStdDeviation;
    }

    @Override
    public InetAddress getTargetAddress() {
        return _targetAddress;
    }

    @Override
    public String toString() {
        return "Ping {\n" +
                "\tpackets sent: " + _packetsSent + "\n" +
                "\tpackets lost: " + _packetsLost + " (" + (_packetsLost / _packetsSent) + "%)\n" +
                "\tmin rtt: " + _minRtt + "\n" +
                "\tmax rtt: " + _maxRtt + "\n" +
                "\tavg rtt: " + _avgRtt + "\n" +
                "\tstd dev rtt: " + _rttStdDeviation + "\n" +
            "}";
    }
}
