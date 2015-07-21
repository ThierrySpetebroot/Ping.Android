package fr.inria.sop.diana.qoe.pingandroid;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by User on 17/07/2015.
 */
public interface IPingCommand {
    public boolean isRunning();

    public IPingResult execute(InetAddress address) throws InterruptedException, IOException;
    public void cancel();
}
