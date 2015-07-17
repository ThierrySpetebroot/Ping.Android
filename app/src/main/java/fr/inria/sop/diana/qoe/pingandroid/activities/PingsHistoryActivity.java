package fr.inria.sop.diana.qoe.pingandroid.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.UUID;

import fr.inria.sop.diana.qoe.pingandroid.R;
import fr.inria.sop.diana.qoe.pingandroid.config.StaticRemoteResourcesEnum;


public class PingsHistoryActivity extends Activity {

    public static final String SELECTED_ITEM_EXTRA = "selected_item";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pings_history);

        // customize UI
        setTitle("Pings History");

        // get IDS asych
        new PingsHistoryTask().execute();
    }

    private HttpURLConnection getConnection() throws IOException {
        HttpURLConnection connection;

        connection = (HttpURLConnection) StaticRemoteResourcesEnum.PINGS.getURL().openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestMethod("GET");

        return connection;
    }

    private UUID[] getPingSessionsUuids() {
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
                    JSONArray array = new JSONArray(response.toString());

                    UUID[] ids = new UUID[array.length()];
                    for (int i = 0; i < array.length(); i++) {
                        ids[i] = UUID.fromString(array.getString(i));
                    }
                    return ids;
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("Pings History", "Illegal JSON");
                    this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(PingsHistoryActivity.this, "Impossible to load the Pings History (Invalid result from Server).", Toast.LENGTH_LONG);
                        }
                    });
                }
            } else {
                Log.e("Pings History", "GET request failed with code " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Pings History", "Impossible to load the Pings History (no connection?).");
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(PingsHistoryActivity.this, "Impossible to load the Pings History. Please check your connection.", Toast.LENGTH_LONG);
                }
            });
        }
        return new UUID[0];
    }

    private void initUI(final UUID[] uuids) {
        ArrayAdapter<UUID> pingsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, uuids);
        ListView pingsView = (ListView) findViewById(R.id.pingsView);
        pingsView.setAdapter(pingsAdapter);
        pingsView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // selected item
                UUID selectedItem = uuids[position];
                Log.i("Pings History", "Selected Item " + selectedItem);

                // open new Activity
                Intent i = new Intent(PingsHistoryActivity.this, PingHistoryActivity.class);
                i.putExtra(SELECTED_ITEM_EXTRA, selectedItem);
                startActivity(i);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pings_history, menu);
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

    protected class PingsHistoryTask extends AsyncTask<Void, Integer, UUID[]> {

        @Override
        protected UUID[] doInBackground(Void... params) {
            // retrieve Ping Sessions Ids
            return getPingSessionsUuids();
        }

        @Override
        protected void onPostExecute(UUID[] uuids) {
            super.onPostExecute(uuids);

            // populate view with results
            // push values in UI
            initUI(uuids);
        }
    }
}
