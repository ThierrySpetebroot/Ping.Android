package fr.inria.sop.diana.qoe.pingandroid.commands.ping;

import java.net.InetAddress;

/**
 * Created by User on 13/07/2015.
 */
public interface IPingResult {
    int getPacketsLost();
    int getPacketSent();
    int getMinRtt();
    int getAvgRtt();
    int getMaxRtt();
    InetAddress getTargetAddress();
}
