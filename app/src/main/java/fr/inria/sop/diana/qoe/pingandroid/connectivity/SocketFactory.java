package fr.inria.sop.diana.qoe.pingandroid.connectivity;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

/**
 * Created by User on 16/07/2015.
 */
public class SocketFactory {

    private HashMap<IRemoteDestination, Socket> _socketMap = new HashMap<>();

    public synchronized Socket getSocket(IRemoteDestination destination) throws IOException {
        Socket socket;
        if(!_socketMap.containsKey(destination)) {
            // socket does not exist
            socket = new Socket(destination.getAddress(), destination.getPort());
            _socketMap.put(destination, socket);
        } else {
            socket = _socketMap.get(destination);

            if(socket.isClosed()) {
                _socketMap.remove(destination);
                return getSocket(destination);
            }
        }
        return socket;
    }

    public synchronized void removeSocket(IRemoteDestination destination) {
        try {
            Socket socket = _socketMap.get(destination);
            _socketMap.remove(destination);
            socket.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
