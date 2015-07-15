package fr.inria.sop.diana.qoe.pingandroid;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Service that perform cyclic ping until stop is requested.
 */
public class PingService extends Service {

    private boolean isPinging = false;
    private boolean isStopRequested = false;

    protected PingCommand command;
    protected int ms;

    protected IBinder binder = new PingServiceBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    protected class PingServiceBinder extends Binder {
        public boolean isPinging() {
            return isPinging;
        }

        public void init(PingCommand c, int repeat_every_ms) {
            if(isPinging()) {
                throw new IllegalStateException("Cannot change command while pinging, call stopPing() before.");
            }

            command = c;
            ms = repeat_every_ms;
        }

        public void startPing(final InetAddress address) {
            isPinging = true;
            isStopRequested = false;
            Log.i("Ping Service", "Starting Ping Session");
            // create thread
            Runnable behaviour = new Runnable() {
                @Override
                public void run() {
                    notifyPingSessionStartedEvent();

                    while(!isStopRequested) {
                        try {
                            IPingResult result = command.execute(address);
                            notifyPingCompletedEvent(result);
                        } catch (InterruptedException e) {
                            // Ping Stopped, stop requested externally
                            break;
                        }
                        try {
                            Thread.sleep(ms);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    isPinging = false;
                }
            };
            Thread th = new Thread(behaviour);

            // start pinging operation
            th.start();
        }

        public void stopPing() {
            isStopRequested = true;
            command.cancel();
        }

        // observer pattern
        private List<IPingCompletedEventHandler> pingCompletedObservers = new ArrayList<IPingCompletedEventHandler>();
        public void registerPingCompletedEventHandler(IPingCompletedEventHandler eventHandler) {
            pingCompletedObservers.add(eventHandler);
        }

        public void unregisterPingCompletedEventHandler(IPingCompletedEventHandler eventHandler) {
            pingCompletedObservers.remove(eventHandler);
        }

        protected void notifyPingCompletedEvent(IPingResult result) {
            for(IPingCompletedEventHandler observer : pingCompletedObservers) {
                observer.onPingCompleted(this, result);
            }
        }

        private List<IPingSessionStartedEventHandler> pingSessionStartedObservers = new ArrayList<IPingSessionStartedEventHandler>();
        public void registerPingSessionStartedEventHandler(IPingSessionStartedEventHandler eventHandler) {
            pingSessionStartedObservers.add(eventHandler);
        }

        public void unregisterPingSessionStartedEventHandler(IPingSessionStartedEventHandler eventHandler) {
            pingSessionStartedObservers.remove(eventHandler);
        }

        protected void notifyPingSessionStartedEvent() {
            for(IPingSessionStartedEventHandler observer : pingSessionStartedObservers) {
                observer.onPingSessionStarted(this);
            }
        }
    }
}
