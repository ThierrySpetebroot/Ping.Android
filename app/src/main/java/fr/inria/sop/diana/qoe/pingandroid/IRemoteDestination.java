package fr.inria.sop.diana.qoe.pingandroid;

import java.net.InetAddress;

/**
 * Created by User on 13/07/2015.
 */
public interface IRemoteDestination extends IDestination {
    InetAddress getAddress();
}
