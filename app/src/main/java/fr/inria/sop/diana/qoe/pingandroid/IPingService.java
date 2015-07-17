package fr.inria.sop.diana.qoe.pingandroid;

import java.net.InetAddress;

public interface IPingService {
    boolean isPinging();

    void init(IPingCommand c, int repeatEveryMs);
    void startPing(final InetAddress address);
    void stopPing();

    void registerPingCompletedEventHandler(IPingCompletedEventHandler eventHandler);
    void unregisterPingCompletedEventHandler(IPingCompletedEventHandler eventHandler);

    void registerPingSessionStartedEventHandler(IPingSessionStartedEventHandler eventHandler);
    void unregisterPingSessionStartedEventHandler(IPingSessionStartedEventHandler eventHandler);
}
