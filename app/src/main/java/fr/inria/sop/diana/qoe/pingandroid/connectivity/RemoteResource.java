package fr.inria.sop.diana.qoe.pingandroid.connectivity;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by User on 17/07/2015.
 */
public class RemoteResource implements IRemoteResource {

    private String protocol;
    private String path;
    private IRemoteDestination destination;
    private URL url;

    public RemoteResource(String protocol, IRemoteDestination destination, String path) {
        this.protocol = protocol;
        this.destination = destination;
        this.path = path;
        try {
            this.url = new URL(protocol, destination.getAddress().getHostAddress(), destination.getPort(), path);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public IRemoteDestination getDestination() {
        return destination;
    }

    @Override
    public URL getURL() {
        return url;
    }
}
