package fr.inria.sop.diana.qoe.pingandroid;

import android.util.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

/**
 * Created by User on 13/07/2015.
 */
public enum RemoteDestinationsEnum implements IRemoteDestination {
    STORAGE_SERVER("Storage Server", AppConfiguration.STORAGE_SERVER_ADDRESS);

    private String _name;
    private UUID _id = UUID.randomUUID();

    private InetAddress _address;

    RemoteDestinationsEnum(String name) {
        _name = name;
    }

    RemoteDestinationsEnum(String name, InetAddress address) {
        _name = name;
        _address = address;
    }

    RemoteDestinationsEnum(String name, String hostname) {
        try {
            _name = name;
            _address = InetAddress.getByName(hostname);
        } catch (UnknownHostException e) {
            Log.e("Remote Destinations", "Cannot resolve hostname " + name + ": " + hostname);
            e.printStackTrace();
        }
    }

    public InetAddress getAddress() {
        return _address;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public UUID getId() {
        return _id;
    }
}
