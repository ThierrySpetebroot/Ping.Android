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
import android.widget.TextView;
import android.widget.Toast;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import fr.inria.sop.diana.qoe.pingandroid.tasks.SavePingResultTask;


public class MainActivity extends Activity {

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
                pingService.init(new PingCommand(), 5000);

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
    public class PlotHandler implements IPingCompletedEventHandler, IPingSessionStartedEventHandler {

        private TextView out = null;

        public void onPingSessionStarted(PingService.PingServiceBinder source) {
            // initialize Results UI #TODO
            Log.i("MAIN", "PING SESSION STARTED");
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    RelativeLayout layout = (RelativeLayout) findViewById(R.id.results_layout);
                    layout.removeAllViews();

                    out = new TextView(MainActivity.this);
                    layout.addView(out);
                }
            });
        }

        @Override
        public void onPingCompleted(PingService.PingServiceBinder source, final IPingResult result) {
            // append point in graph
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    out.append(result.toString());
                    Toast.makeText(MainActivity.this, "PING", Toast.LENGTH_SHORT).show();
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
