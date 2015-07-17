package fr.inria.sop.diana.qoe.pingandroid.tasks;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;

import fr.inria.sop.diana.qoe.pingandroid.MainActivity;
import fr.inria.sop.diana.qoe.pingandroid.IPingResult;

/**
 * Save PingResults asynchronously
 */
public class SavePingResultTask extends AsyncTask<IPingResult, Integer, Void> {

    protected MainActivity activity; // activity that launched the Task

    protected URL url;

    public SavePingResultTask(MainActivity activity, URL url) {
        this.url = url;
        this.activity = activity;
    }

    @Override
    protected Void doInBackground(IPingResult... results) {
        // N.B.: HttpURLConnection is single use (only 1 request can be performed with an instance)

        // init connection
        HttpURLConnection connection;
        try {
            connection = getConnection();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Save PingResult Task", "IOException, impossible to open a connection to the storage server @" + url);
            activity.showError("impossible to open a connection with " + url);
            return null;
        }

        // send data
        try {
            sendResults(connection, results);
            Log.i("Save PingResult Task", "Ping Results successfully sent to storage server @" + url);
        }
        catch (IOException e) {
            Log.e("Save PingResult Task", "IOException, impossible to send the data to the storage server @" + url);
            activity.showError("impossible to send the data to the storage server @" + url);
            e.printStackTrace();
        }
        return null;
    }

    private void sendResults(HttpURLConnection connection, IPingResult[] results) throws IOException {
        // setup request
        JSONObject requestParams = new JSONObject();
        try {
            requestParams.put("uuid", activity.getCurrentSessionId().toString());
            requestParams.put("data", toJson(results));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i("Storage Task", requestParams.toString());
        byte[] rawRequestParams = ("ping=" + requestParams.toString()).getBytes("UTF-8");

        // push request into request buffer
        OutputStream os = connection.getOutputStream();
        os.write(rawRequestParams);
        os.flush();
        os.close();

        // check response code
        int responseCode = connection.getResponseCode();
        if(responseCode == HttpURLConnection.HTTP_OK) {
            // handle response
            connection.getInputStream(); // N.B.: always required (request not performed otherwise)
        } else {
            Log.e("Task", "POST request failed with code " + responseCode);
        }
    }

    private JSONArray toJson(IPingResult[] results) {
        JSONArray array = new JSONArray();

        for(IPingResult result : results) {
            JSONObject resultObject = new JSONObject();
            try {
                resultObject.put("min_rtt", result.getMinRtt());
                resultObject.put("max_rtt", result.getMaxRtt());
                resultObject.put("avg_rtt", result.getAvgRtt());
                resultObject.put("dev_rtt", result.getRttMeanDeviation());

                resultObject.put("pkt_sent", result.getPacketSent());
                resultObject.put("pkt_lost", result.getPacketsLost());

                resultObject.put("address", result.getTargetAddress().getHostAddress());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            array.put(resultObject);
        }

        return array;
    }

    private HttpURLConnection getConnection() throws IOException {
        HttpURLConnection connection;

        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent","Mozilla/5.0");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        return connection;
    }
}
