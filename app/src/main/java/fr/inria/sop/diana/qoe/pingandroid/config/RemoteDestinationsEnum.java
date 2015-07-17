package fr.inria.sop.diana.qoe.pingandroid.config;

import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import fr.inria.sop.diana.qoe.pingandroid.connectivity.IRemoteDestination;

/**
 * Created by User on 13/07/2015.
 */
public enum RemoteDestinationsEnum implements IRemoteDestination {
    STORAGE_SERVER("Storage Server", AppConfiguration.STORAGE_SERVER_ADDRESS, AppConfiguration.STORAGE_SERVER_PORT);

    private String _name;
    private UUID _id = UUID.randomUUID();

    private InetAddress _address;
    private int _port;

    RemoteDestinationsEnum(String name) {
        _name = name;
    }

    RemoteDestinationsEnum(String name, InetAddress address) {
        _name = name;
        _address = address;
    }

    RemoteDestinationsEnum(String name, String hostname, int port) {
        try {
            _name = name;
            _address = InetAddress.getByName(hostname);
            _port = port;
        } catch (UnknownHostException e) {
            Log.e("Remote Destinations", "Cannot resolve hostname " + name + ": " + hostname + ":" + port);
            e.printStackTrace();
        }
    }

    public InetAddress getAddress() {
        return _address;
    }

    public int getPort() { return _port; }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public UUID getId() {
        return _id;
    }
}
