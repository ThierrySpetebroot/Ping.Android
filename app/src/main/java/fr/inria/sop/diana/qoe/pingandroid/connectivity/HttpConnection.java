package fr.inria.sop.diana.qoe.pingandroid.connectivity;

import java.io.IOException;
import java.net.URLConnection;

import fr.inria.sop.diana.qoe.pingandroid.IConnection;

/**
 * Created by User on 17/07/2015.
 */
public class HttpConnection implements IConnection<String> {

    URLConnection connection;

    @Override
    public String receive() throws IOException {
        return null;
    }

    @Override
    public void send(String value) throws IOException {

    }

    @Override
    public void close() {

    }
}
