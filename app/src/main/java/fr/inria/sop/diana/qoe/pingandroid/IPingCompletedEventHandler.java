package fr.inria.sop.diana.qoe.pingandroid;

/**
 * Created by User on 13/07/2015.
 */
public interface IPingCompletedEventHandler {
    void onPingCompleted(PingService.PingServiceBinder source, IPingResult result);
}
