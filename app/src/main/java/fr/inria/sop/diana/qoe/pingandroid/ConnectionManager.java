package fr.inria.sop.diana.qoe.pingandroid;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ConnectionManager implements IConnectionManager<IPingResult, IRemoteDestination> {
    private Map<UUID, IConnection<IPingResult>> connectionsById                  = new HashMap<>();
    private Map<IDestination, IConnection<IPingResult>> connectionsByDestination = new HashMap<>();

    ConnectionManager(IRemoteDestination[] destinations) {
        for(IRemoteDestination destination : destinations) {
            // open connection #TODO



            // store it in the flyweight maps #TODO

        }
    }

    @Override
    public IConnection<IPingResult> getConnection(IRemoteDestination d) {
        return connectionsByDestination.get(d);
    }
}
