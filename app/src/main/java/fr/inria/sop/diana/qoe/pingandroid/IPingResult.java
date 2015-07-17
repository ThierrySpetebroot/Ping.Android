package fr.inria.sop.diana.qoe.pingandroid;

import java.io.Serializable;
import java.net.InetAddress;

/**
 * Created by User on 13/07/2015.
 */
public interface IPingResult extends Serializable {
    int getPacketsLost();
    int getPacketSent();
    float getMinRtt();
    float getAvgRtt();
    float getMaxRtt();
    float getRttMeanDeviation();
    InetAddress getTargetAddress();
}
