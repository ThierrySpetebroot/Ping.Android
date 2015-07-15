package fr.inria.sop.diana.qoe.pingandroid.tasks;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import fr.inria.sop.diana.qoe.pingandroid.IConnectionManager;
import fr.inria.sop.diana.qoe.pingandroid.IConnection;
import fr.inria.sop.diana.qoe.pingandroid.IDestination;
import fr.inria.sop.diana.qoe.pingandroid.IPingResult;
import fr.inria.sop.diana.qoe.pingandroid.RemoteDestinationsEnum;

/**
 * Save PingResults asynchronously
 */
public class SavePingResultTask extends AsyncTask<IPingResult, Integer, Void> {

    protected IConnection<IPingResult> connection;

    public SavePingResultTask(IConnection<IPingResult> connection) {
        this.connection = connection;
    }

    public SavePingResultTask(IConnectionManager<IPingResult, IDestination> connectionManager) {
        connection = connectionManager.getConnection(RemoteDestinationsEnum.STORAGE_SERVER);
    }

    @Override
    protected Void doInBackground(IPingResult... params) {
        try {
            for (IPingResult result : params) {
                connection.send(result);
            }
        }
        catch (IOException e) {
            Log.e("Save PingResult Task", "IOException, impossible to send the data to the storage server");
            e.printStackTrace();
        }
        return null;
    }

    public void close() {
        connection.close();
    }
}
