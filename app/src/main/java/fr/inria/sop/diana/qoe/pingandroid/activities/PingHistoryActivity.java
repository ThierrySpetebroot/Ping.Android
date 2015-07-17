package fr.inria.sop.diana.qoe.pingandroid.activities;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.UUID;

import fr.inria.sop.diana.qoe.pingandroid.IPingResult;
import fr.inria.sop.diana.qoe.pingandroid.PingResult;
import fr.inria.sop.diana.qoe.pingandroid.R;
import fr.inria.sop.diana.qoe.pingandroid.config.RemoteDestinationsEnum;
import fr.inria.sop.diana.qoe.pingandroid.config.StaticRemoteResourcesEnum;
import fr.inria.sop.diana.qoe.pingandroid.connectivity.RemoteResource;


public class PingHistoryActivity extends Activity {

    private LineChart plotRtt;
    private LineChart plotLoss;
    private TextView textOutput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ping_history);

        // init chart references
        plotRtt = (LineChart) findViewById(R.id.rtt_history);
        plotLoss = (LineChart) findViewById(R.id.loss_history);
        textOutput = (TextView) findViewById(R.id.text_history);

        // customize charts
        customizeCharts();

        // get id to visualize
        UUID currentPingId = getSelectedItem();
        Log.i("Ping History", "Showing Ping " + currentPingId);

        // customize UI
        setTitle("Ping History - " + currentPingId);

        // retrieve ping data
        IPingResult[] pingResults = getPingResults(currentPingId);

        // plot data
        plot(pingResults);
    }

    private void customizeCharts() {
        // customize charts
        plotRtt.setDescription("RTT plot (ms)");
        plotLoss.setDescription("Loss Rate plot (%)");
    }

    private UUID getSelectedItem() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            return (UUID) extras.get(PingsHistoryActivity.SELECTED_ITEM_EXTRA);
        }
        return null;
    }

    private IPingResult[] getPingResults(UUID currentPingId) {
        return new IPingResult[0];
    }

    private void plot(IPingResult[] pingResults) {
        // init TabHost
        setupTabs();

        // retrieve data asynch (UI not blocked)
        new PingHistoryTask().execute();
    }

    private void setupTabs() {
        TabHost tabHost = (TabHost) findViewById(R.id.pingTabHost);
        tabHost.setup();

        TabHost.TabSpec rttTab = tabHost.newTabSpec("RTT");
        rttTab.setContent(R.id.rtt_history);
        rttTab.setIndicator("RTT");
        tabHost.addTab(rttTab);

        TabHost.TabSpec lossTab = tabHost.newTabSpec("Loss");
        lossTab.setContent(R.id.loss_history);
        lossTab.setIndicator("LOSS");
        tabHost.addTab(lossTab);

        TabHost.TabSpec rawTab = tabHost.newTabSpec("Raw");
        rawTab.setContent(R.id.text_history);
        rawTab.setIndicator("RAW");
        tabHost.addTab(rawTab);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ping_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_back:
                setResult(RESULT_OK);
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected class PingHistoryTask extends AsyncTask<Void, Void, IPingResult[]> {

        private static final int AVG_RTT_DATA_SET_INDEX = 0;
        private static final int MIN_RTT_DATA_SET_INDEX = 1;
        private static final int MAX_RTT_DATA_SET_INDEX = 2;
        private static final int LOSS_DATA_SET_INDEX = 0;

        private static final int WINDOW_SIZE = 15;

        private HttpURLConnection getConnection() throws IOException {
            RemoteResource remoteResource = new RemoteResource("http", StaticRemoteResourcesEnum.PINGS.getDestination(), StaticRemoteResourcesEnum.PINGS.getPath() + getSelectedItem());

            HttpURLConnection connection;

//            connection = (HttpURLConnection) remoteResource.getURL().openConnection();
            connection = (HttpURLConnection) new URL("http:/" + RemoteDestinationsEnum.STORAGE_SERVER.getAddress() + ":" + RemoteDestinationsEnum.STORAGE_SERVER.getPort() + "/pings/" + getSelectedItem()).openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestMethod("GET");

            return connection;
        }

        @Override
        protected IPingResult[] doInBackground(Void... params) {
            try {
                // create HTTP connection
                HttpURLConnection connection = getConnection();

                // check response code
                int responseCode = connection.getResponseCode();
                if(responseCode == HttpURLConnection.HTTP_OK) {
                    // handle response
                    InputStream is = connection.getInputStream(); // N.B.: always required (request not performed otherwise)
                    BufferedReader in = new BufferedReader(new InputStreamReader(is));

                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    try {
                        Log.i("Alina's Ping History", response.toString());

                        JSONObject res = new JSONObject(response.toString());

                        JSONArray resultsJson = res.getJSONArray("data");
                        IPingResult[] results = new IPingResult[resultsJson.length()];
                        for(int i = 0; i < resultsJson.length(); i++) {
                            results[i] = parseJson(resultsJson.getJSONObject(i));

                            if(results[i] == null) {
                                // error
                                Log.e("Ping History", "Impossible to create PingResult object");
                                return new IPingResult[0];
                            }
                        }
                        return results;

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("Ping History", "Illegal JSON");
                        PingHistoryActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(PingHistoryActivity.this, "Impossible to load the Ping History (Invalid result from Server).", Toast.LENGTH_LONG);
                            }
                        });
                    }
                } else {
                    Log.e("Ping History", "GET request failed with code " + responseCode);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("Ping History", "Impossible to load the Ping History (no connection?).");
                PingHistoryActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(PingHistoryActivity.this, "Impossible to load the Ping History. Please check your connection.", Toast.LENGTH_LONG);
                    }
                });
            }

            return new IPingResult[0];
        }

        protected IPingResult parseJson(JSONObject json) {
            try {
                return new PingResult(
                    json.getInt("pkt_sent"),
                    json.getInt("pkt_lost"),
                    (float)json.getDouble("min_rtt"),
                    (float)json.getDouble("max_rtt"),
                    (float)json.getDouble("avg_rtt"),
                    (float)json.getDouble("dev_rtt"),
                    InetAddress.getByName(json.getString("address"))
                );
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(IPingResult[] iPingResults) {
            super.onPostExecute(iPingResults);

            // set chart data
            initDataCollections(iPingResults);

            // render plot
            drawPlots();
        }

        private void drawPlots() {
            drawPlot(plotRtt);
            drawPlot(plotLoss);
        }

        private void drawPlot(LineChart plot) {
            plot.notifyDataSetChanged();
            plot.invalidate();

            // set fixed window size
            plot.setVisibleXRange(WINDOW_SIZE);
            plot.moveViewToX(plot.getXValCount() - WINDOW_SIZE);
        }

        private int getColor(int key) {
            return PingHistoryActivity.this.getResources().getColor(key);
        }

        private void setDataSetStyle(LineDataSet dataSet, int lineColor) {
            dataSet.setLineWidth(4f);
            dataSet.setCircleColor(lineColor);
            dataSet.setColor(lineColor);
        }

        private void initDataCollections(IPingResult[] results) {
            // prepare RTT chart
            ArrayList<Entry> avgRttEntries = new ArrayList<>();
            ArrayList<Entry> minRttEntries = new ArrayList<>();
            ArrayList<Entry> maxRttEntries = new ArrayList<>();
            LineDataSet avgRttSet = new LineDataSet(avgRttEntries, "Avg RTT");
            LineDataSet minRttSet = new LineDataSet(minRttEntries, "Min RTT");
            LineDataSet maxRttSet = new LineDataSet(maxRttEntries, "Max RTT");
            setDataSetStyle(avgRttSet, getColor(R.color.avg_rtt));
            setDataSetStyle(minRttSet, getColor(R.color.min_rtt));
            setDataSetStyle(maxRttSet, getColor(R.color.max_rtt));
            LineData dataRtt = new LineData();
            dataRtt.addDataSet(avgRttSet);
            dataRtt.addDataSet(minRttSet);
            dataRtt.addDataSet(maxRttSet);


            // prepare loss chart
            ArrayList<Entry> lossEntries = new ArrayList<>();
            LineDataSet lossSet = new LineDataSet(lossEntries, "Loss rate");
            setDataSetStyle(lossSet, getColor(R.color.loss));
            LineData dataLoss = new LineData();
            dataLoss.addDataSet(lossSet);


            // prepare raw output view
            textOutput.setText("");

            // insert data to fill the UI
            for (int i = 0; i < results.length; i++) {
                IPingResult r = results[i];

                dataRtt.addXValue("" + i);
                dataRtt.addEntry(new Entry(r.getAvgRtt(), i), AVG_RTT_DATA_SET_INDEX);
                dataRtt.addEntry(new Entry(r.getMinRtt(), i), MIN_RTT_DATA_SET_INDEX);
                dataRtt.addEntry(new Entry(r.getMaxRtt(), i), MAX_RTT_DATA_SET_INDEX);

                dataLoss.addXValue("" + i);
                dataLoss.addEntry(new Entry(((float) r.getPacketsLost() * 100) / r.getPacketSent(), i), LOSS_DATA_SET_INDEX);

                textOutput.append(i + ". " + r.toString() + "\n");
            }

            // binding data
            plotRtt.setData(dataRtt);
            plotLoss.setData(dataLoss);
        }
    }
}
