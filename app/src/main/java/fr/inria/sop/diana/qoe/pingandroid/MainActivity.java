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
import android.widget.RelativeLayout;
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

        // create Connection Manager
        // IConnectionManager<IPingResult, ? extends IDestination> connectionManager = new ConnectionManager(new IRemoteDestination[] { RemoteDestinationsEnum.STORAGE_SERVER });

        // create Ping Service Daemon
        startPingService();
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

        private static final float WINDOW_SIZE = 15;
        private static final int AVG_RTT_DATA_SET_INDEX = 0;
        private static final int MIN_RTT_DATA_SET_INDEX = 1;
        private static final int MAX_RTT_DATA_SET_INDEX = 2;

        private int getColor(int key) {
            return MainActivity.this.getResources().getColor(key);
        }

        private LineChart plot;

        private ArrayList<Entry> avgRttEntries;
        private ArrayList<Entry> minRttEntries;
        private ArrayList<Entry> maxRttEntries;
        private LineData data;

        public PlotHandler() {
            // prepare color map
            if(DATA_SET_COLORS == null) {
                DATA_SET_COLORS = new HashMap<>();
                DATA_SET_COLORS.put(AVG_RTT_DATA_SET_INDEX, getColor(R.color.avg_rtt));
                DATA_SET_COLORS.put(MIN_RTT_DATA_SET_INDEX, getColor(R.color.min_rtt));
                DATA_SET_COLORS.put(MAX_RTT_DATA_SET_INDEX, getColor(R.color.max_rtt));
            }
        }

        private void appendPointToChart() {
            // TODO
        }

        private void setDataSetStyle(LineDataSet dataSet, int dataSetIndex) {
            dataSet.setLineWidth(4f);
            int lineColor = DATA_SET_COLORS.get(dataSetIndex);
            dataSet.setCircleColor(lineColor);
            dataSet.setColor(lineColor);
        }

        public void onPingSessionStarted(PingService.PingServiceBinder source) {
            // initialize Results UI #TODO
            Log.i("MAIN", "PING SESSION STARTED");
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // reset Layout
                    RelativeLayout layout = (RelativeLayout) findViewById(R.id.results_layout);
                    layout.removeAllViews();

                    // create LineChart
                    plot = new LineChart(MainActivity.this);
                    plot.setDescription("RTT plot (ms)");
                    layout.addView(plot);

                    // chart customization
                    plot.setNoDataText("Pinging...");
                    plot.setNoDataTextDescription("Pinging...");

                    // init data collections
                    avgRttEntries = new ArrayList<>();
                    LineDataSet avgRttSet = new LineDataSet(avgRttEntries, "Avg RTT");
                    LineDataSet minRttSet = new LineDataSet(minRttEntries, "Min RTT");
                    LineDataSet maxRttSet = new LineDataSet(maxRttEntries, "Max RTT");
                    setDataSetStyle(avgRttSet, AVG_RTT_DATA_SET_INDEX);
                    setDataSetStyle(minRttSet, MIN_RTT_DATA_SET_INDEX);
                    setDataSetStyle(maxRttSet, MAX_RTT_DATA_SET_INDEX);
                    data = new LineData();
                    data.addDataSet(avgRttSet);
                    data.addDataSet(minRttSet);
                    data.addDataSet(maxRttSet);

                    for(int i = 0; i < WINDOW_SIZE; i++) {
                        data.addXValue("");
                        data.addEntry(new Entry(0, i), AVG_RTT_DATA_SET_INDEX);
                        data.addEntry(new Entry(0, i), MIN_RTT_DATA_SET_INDEX);
                        data.addEntry(new Entry(0, i), MAX_RTT_DATA_SET_INDEX);
                    }
                    plot.setData(data);
                }
            });
        }

        @Override
        public void onPingCompleted(PingService.PingServiceBinder source, final IPingResult result) {
            // append point in graph
            MainActivity.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    // Ping Toast
                    Toast.makeText(MainActivity.this, "PING", Toast.LENGTH_SHORT).show();

                    //LineDataSet set = data.getDataSetByIndex(AVG_RTT_DATA_SET_INDEX);

                    // Append Point
                    int index = avgRttEntries.size();
                    data.addXValue("");
                    data.addEntry(new Entry(result.getAvgRtt(), index), AVG_RTT_DATA_SET_INDEX);
                    data.addEntry(new Entry(result.getMinRtt(), index), MIN_RTT_DATA_SET_INDEX);
                    data.addEntry(new Entry(result.getMaxRtt(), index), MAX_RTT_DATA_SET_INDEX);

                    // remove obsolete data
//                    if(avgRttEntries.size() > WINDOW_SIZE) {
//                        data.removeEntry(0, AVG_RTT_DATA_SET_INDEX);
//                    }

                    // redraw Plot
                    plot.notifyDataSetChanged();
                    plot.invalidate();

                    // set fixed window size
                    plot.setVisibleXRange(WINDOW_SIZE);
                    plot.moveViewToX(data.getXValCount() - WINDOW_SIZE);
                }
            });

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
