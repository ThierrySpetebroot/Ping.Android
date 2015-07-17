package fr.inria.sop.diana.qoe.pingandroid.connectivity;

import java.net.URL;

public interface IRemoteResource {
    String getProtocol();
    String getPath();
    IRemoteDestination getDestination();
    URL getURL();
}
