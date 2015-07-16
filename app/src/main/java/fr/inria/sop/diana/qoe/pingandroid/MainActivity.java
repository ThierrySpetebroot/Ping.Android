package fr.inria.sop.diana.qoe.pingandroid;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import fr.inria.sop.diana.qoe.pingandroid.tasks.SavePingResultTask;


public class MainActivity extends Activity {

    private static final int PING_WAIT_TIME = 500;

    public InetAddress targetAddress = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // setup UI
        setContentView(R.layout.activity_main);
        setupIpTextInput();
        findViewById(R.id.results_layout).setVisibility(View.INVISIBLE);
        setupTabs();

        // create Connection Manager
        // IConnectionManager<IPingResult, ? extends IDestination> connectionManager = new ConnectionManager(new IRemoteDestination[] { RemoteDestinationsEnum.STORAGE_SERVER });

        // create Ping Service Daemon
        startPingService();
    }

    private void setupTabs() {
        TabHost tabHost = (TabHost) findViewById(R.id.tabHost);
        tabHost.setup();

        TabHost.TabSpec rttTab = tabHost.newTabSpec("RTT");
        rttTab.setContent(R.id.rtt_view);
        rttTab.setIndicator("RTT");
        tabHost.addTab(rttTab);

        TabHost.TabSpec lossTab = tabHost.newTabSpec("Loss");
        lossTab.setContent(R.id.loss_view);
        lossTab.setIndicator("LOSS");
        tabHost.addTab(lossTab);

        TabHost.TabSpec rawTab = tabHost.newTabSpec("Raw");
        rawTab.setContent(R.id.text_view);
        rawTab.setIndicator("RAW");
        tabHost.addTab(rawTab);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(pingServiceBounded) {
            unbindService(pingServiceConnection);
            pingServiceBounded = false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        // #TODO handle all actions (define History tab)
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Ping Service definition
    protected boolean pingServiceBounded = false;
    protected PingService.PingServiceBinder pingService = null;
    protected ServiceConnection pingServiceConnection = null;
    protected IPingCompletedEventHandler[] pingCompletedObservers;
    protected IPingSessionStartedEventHandler[] pingSessionStartedObservers = new IPingSessionStartedEventHandler[] { };

    private void registerPingServiceHandlers() {
        for (IPingCompletedEventHandler observer : pingCompletedObservers) {
            pingService.registerPingCompletedEventHandler(observer);
        }
        for (IPingSessionStartedEventHandler observer : pingSessionStartedObservers) {
            pingService.registerPingSessionStartedEventHandler(observer);
        }
    }

    private void unregisterPingServiceHandlers() {
        for (IPingCompletedEventHandler observer : pingCompletedObservers) {
            pingService.unregisterPingCompletedEventHandler(observer);
        }
        for (IPingSessionStartedEventHandler observer : pingSessionStartedObservers) {
            pingService.unregisterPingSessionStartedEventHandler(observer);
        }
    }

    private void startPingService() {
        Log.i("Main", "Starting Ping Service");

        // event handler definition
        PlotHandler handler = new PlotHandler();
        pingCompletedObservers = new IPingCompletedEventHandler[] {
                new PersistenceHandler(), handler
        };
        pingSessionStartedObservers = new IPingSessionStartedEventHandler[] {
                handler
        };

        // define connection to service
        pingServiceConnection = new ServiceConnection() {

            public void onServiceDisconnected(ComponentName name) {
                // Ping Service Disabled
                Toast.makeText(MainActivity.this, "Ping Service is disconnected", Toast.LENGTH_LONG).show();
                Log.i("Main", "Ping Service Disconnected");

                // unregister Ping Service Handlers
                unregisterPingServiceHandlers();

                pingServiceBounded = false;
                pingService = null;
            }

            public void onServiceConnected(ComponentName name, IBinder service) {
                // Ping Service Ready
                // register Ping Service
                Toast.makeText(MainActivity.this, "Ping Service is connected", Toast.LENGTH_LONG).show();
                Log.i("Main", "Ping Service Connected");
                pingServiceBounded = true;
                pingService = (PingService.PingServiceBinder) service;
                pingService.init(new PingCommand(), PING_WAIT_TIME);

                // register Ping Service Handlers
                registerPingServiceHandlers();
            }
        };

        // start service
        Intent i = new Intent(this, PingService.class);
        bindService(i, pingServiceConnection, BIND_AUTO_CREATE);
        startService(i);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // UI Handling
    //  Ping Button
    public void onPingButtonClicked(View view) {
        Button button = (Button) view;

        if(pingService == null) {
            Toast.makeText(MainActivity.this, "Ping Service is not ready. Please wait.", Toast.LENGTH_LONG).show();
            return;
        }
        if(targetAddress == null) {
            Toast.makeText(MainActivity.this, "Please insert a valid IP address.", Toast.LENGTH_LONG).show();
            return;
        }

        if(pingService.isPinging()) {
            // stop probing
            pingService.stopPing();

            // show start probing label
            button.setText(R.string.action_probe__start);

            // enable input
            enableIpInput();
        } else {
            // start probing;
            pingService.startPing(targetAddress);

            // show stop probing label
            button.setText(R.string.action_probe__stop);

            // disable input
            disableIpInput();
        }
    }

    //  IP Text Input
    protected void disableIpInput() {
        EditText ipInput = (EditText) findViewById(R.id.target_ip_input);
        ipInput.setBackgroundColor(getResources().getColor(R.color.no_background));
        ipInput.setEnabled(false);
    }

    protected void enableIpInput() {
        EditText ipInput = (EditText) findViewById(R.id.target_ip_input);
        ipInput.setEnabled(true);
    }

    private void setupIpTextInput() {
        // get Target Ip EditText element
        final EditText ipTextInput = (EditText) findViewById(R.id.target_ip_input);

        // set Input Watcher to sanitize input
        ipTextInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!Patterns.IP_ADDRESS.matcher(s).matches()) {
                    ipTextInput.setError(getResources().getString(R.string.error_invalid_target_ip));
                    ipTextInput.setBackgroundColor(getResources().getColor(R.color.error_background));
                    return;
                }

                try {
                    targetAddress = Inet4Address.getByName(s.toString());
                    ipTextInput.setBackgroundColor(getResources().getColor(R.color.success_background));
                } catch (UnknownHostException e) {
                    // impossible if we provide only IP addresses
                    Log.e("Main Activity", "Unknown Host Exception: " + e.getStackTrace());
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Do nothing
            }
        });
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Ping Service Event Handling
    //  UI
    private static HashMap<Integer, Integer> DATA_SET_COLORS;
    public class PlotHandler implements IPingCompletedEventHandler, IPingSessionStartedEventHandler {

        private static final int WINDOW_SIZE = 15;
        private static final int AVG_RTT_DATA_SET_INDEX = 0;
        private static final int MIN_RTT_DATA_SET_INDEX = 1;
        private static final int MAX_RTT_DATA_SET_INDEX = 2;
        private static final int LOSS_DATA_SET_INDEX = 0;

        private int getColor(int key) {
            return MainActivity.this.getResources().getColor(key);
        }

        private LineChart plotRtt  = (LineChart) findViewById(R.id.rtt_view);
        private LineChart plotLoss = (LineChart) findViewById(R.id.loss_view);
        private TextView  textOutput = (TextView) findViewById(R.id.text_view);

        private LineData dataRtt;
        private LineData dataLoss;

        public PlotHandler() {
            // customize charts
            plotRtt.setDescription("RTT plot (ms)");
            plotRtt.setNoDataText("Pinging...");
            plotRtt.setNoDataTextDescription("Pinging...");

            plotLoss.setDescription("Loss Rate plot (%)");
            plotLoss.setNoDataText("Pinging...");
            plotLoss.setNoDataTextDescription("Pinging...");
        }

        private void appendPointToChart(LineChart chart, int dataSetIndex, float value) {
            // remove obsolete data
            LineData data = chart.getLineData();
            LineDataSet set = data.getDataSetByIndex(dataSetIndex);
            if(set.getEntryCount() >= WINDOW_SIZE) {
                data.removeEntry(0, dataSetIndex);
                slideEntryIndexes(set);
            }

            // add data
            int index = WINDOW_SIZE;
            data.addEntry(new Entry(value, index), dataSetIndex);
        }

        private void appendPointToCharts(IPingResult result) {
            Log.i("PLOT", "Append Point to Charts");
            appendPointToChart(plotRtt, AVG_RTT_DATA_SET_INDEX, result.getAvgRtt());
            appendPointToChart(plotRtt, MIN_RTT_DATA_SET_INDEX, result.getMinRtt());
            appendPointToChart(plotRtt, MAX_RTT_DATA_SET_INDEX, result.getMaxRtt());
            appendPointToChart(plotLoss, LOSS_DATA_SET_INDEX, ((float) result.getPacketsLost()) / result.getPacketSent());
        }

        private void setDataSetStyle(LineDataSet dataSet, int lineColor) {
            dataSet.setLineWidth(4f);
            dataSet.setCircleColor(lineColor);
            dataSet.setColor(lineColor);
        }

        public void onPingSessionStarted(PingService.PingServiceBinder source) {
            // initialize Results UI
            Log.i("MAIN", "PING SESSION STARTED");
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // show Result Layout
                    findViewById(R.id.results_layout).setVisibility(View.VISIBLE);

                    // reset View
                    plotRtt.clear();
                    plotLoss.clear();
                    textOutput.setText("");

                    // init data collections
                    Log.i("PLOT", "Init Data Collections");
                    initDataCollections();
                }
            });
        }

        private void initDataCollections() {
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
            dataRtt = new LineData();
            dataRtt.addDataSet(avgRttSet);
            dataRtt.addDataSet(minRttSet);
            dataRtt.addDataSet(maxRttSet);

            // prepare loss chart
            ArrayList<Entry> lossEntries = new ArrayList<>();
            LineDataSet lossSet = new LineDataSet(lossEntries, "Loss rate");
            setDataSetStyle(lossSet, getColor(R.color.loss));
            dataLoss = new LineData();
            dataLoss.addDataSet(lossSet);

            // generate sample data to fill the UI
            Log.i("PLOT", "Generate Sample Data");
            for (int i = 0; i < WINDOW_SIZE; i++) {
                dataRtt.addXValue("");
                dataRtt.addEntry(new Entry(0, i), AVG_RTT_DATA_SET_INDEX);
                dataRtt.addEntry(new Entry(0, i), MIN_RTT_DATA_SET_INDEX);
                dataRtt.addEntry(new Entry(0, i), MAX_RTT_DATA_SET_INDEX);

                dataLoss.addXValue("");
                dataLoss.addEntry(new Entry(0, i), LOSS_DATA_SET_INDEX);
            }

            plotRtt.setData(dataRtt);
            plotLoss.setData(dataLoss);
        }

        @Override
        public void onPingCompleted(PingService.PingServiceBinder source, final IPingResult result) {
            // append point in graph
            MainActivity.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    Log.i("PLOT", "Ping Completed");
                    // Ping Toast
                    //Toast.makeText(MainActivity.this, "PING", Toast.LENGTH_SHORT).show();

                    // Append Point
                    appendPointToCharts(result);

                    // Append Raw output to Raw Tab
                    if(result instanceof NativePingResult) {
                        textOutput.append(((NativePingResult) result).getRawOutput() + '\n');
                    } else {
                        textOutput.append(result.toString());
                    }

                    // update Charts
                    updateCharts();
                }
            });

        }

        private void updateChart(LineChart chart) {
            // redraw Plot
            chart.notifyDataSetChanged();
            chart.invalidate();

            // set fixed window size
            chart.setVisibleXRange(WINDOW_SIZE);
        }

        private void updateCharts() {
            Log.i("PLOT", "Update Charts");
            updateChart(plotRtt);
            updateChart(plotLoss);
        }

        private void slideEntryIndexes(LineDataSet dataSetByIndex) {
            for(int i = 1; i < WINDOW_SIZE; i++) {
                Entry e = dataSetByIndex.getEntryForXIndex(i);

                if(e == null) continue;

                e.setXIndex(i - 1);
            }
        }
    }

    //  Persistence
    public class PersistenceHandler implements IPingCompletedEventHandler {

        private SavePingResultTask asyncPersistenceTask;

        public PersistenceHandler() {
            // create SavePingResultTask
            // get Flyweight from Factory #TODO
            // #TODO use SavePingResultTask
        }

        @Override
        public void onPingCompleted(PingService.PingServiceBinder source, IPingResult result) {
            // send result to server
            //asyncPersistenceTask.execute(result); #TODO
        }

        public void onPingStopped() {
            // close connection (flush)
            // asyncPersistenceTask.close();
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
}
