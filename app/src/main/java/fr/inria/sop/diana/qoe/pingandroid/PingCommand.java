package fr.inria.sop.diana.qoe.pingandroid;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;


public class PingCommand {
    private static final int REQUEST_NUMBER = 10;

    private static ArrayList<String> BASE_CMD;

    static {
        BASE_CMD = new ArrayList<String>();
        BASE_CMD.add("ping");
    }

    private Process _process;

    private boolean _isRunning = false;
    public synchronized boolean isRunning() {
        return _isRunning;
    }
    protected synchronized void setIsRunning(boolean value) {
        _isRunning = value;
    }

    private String[] readStream(InputStream in, boolean isQuiet) {
        int rowsNumber = isQuiet? 1 + 4 : 1 + REQUEST_NUMBER + 4;
        int currentRowIndex = 0;
        String[] rows = new String[rowsNumber];
        BufferedReader inReader = new BufferedReader(new InputStreamReader(in));

        String s;
        try {
            while ((s = inReader.readLine()) != null) {
                rows[currentRowIndex] = s;
                currentRowIndex++;
            }
            if((currentRowIndex+1) < rowsNumber)
                return null; // process interrupted

            return rows;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public IPingResult execute(InetAddress address) throws InterruptedException {
        if(isRunning()) {
            throw new IllegalStateException("Command already running");
        }

        String[] rows;
        synchronized (this) { // ensure that _process will contain a Process (if cancel is called while execute() is running)
            setIsRunning(true);

            try {
                ArrayList<String> cmd = new ArrayList<>(BASE_CMD);
                cmd.add("-q"); // isQuiet = true
                cmd.add("-c " + REQUEST_NUMBER);
                cmd.add(address.getHostAddress());

                Log.i("Ping Command", "Running Command: " + cmd);

                _process = new ProcessBuilder()
                        .command(cmd)
                        .redirectErrorStream(true)
                        .start();
            } catch (IOException e) {
                Log.e("Ping Command", "IOException: " + e.getStackTrace());
                e.printStackTrace();
                setIsRunning(false);
                return null;
            }
        }

        InputStream in = _process.getInputStream();
//        OutputStream out = _process.getOutputStream();

        rows = readStream(in, true);

        if(rows == null && !isRunning()) {
            throw new InterruptedException("Command Execution interrupted");
        }

        _process.destroy();
        _process = null;

        // parse output
        IPingResult result = parseOutput(rows, address);

        setIsRunning(false);

        return result;
    }

    public void cancel() {
        if(!isRunning() || _process == null) {
            return;
        }

        Log.i("Ping Command", "Cancelled");

        setIsRunning(false);
        _process.destroy();
    }

    private IPingResult parseOutput(String[] rows, InetAddress address) {
        Scanner scanner = new Scanner(rows[rows.length - 2]);
        int packetsSent = scanner.nextInt();
        scanner.skip(" packets transmitted, ");
        int packetsReceived = scanner.nextInt();

        scanner = new Scanner(rows[rows.length - 1]);
        scanner.useLocale(Locale.ENGLISH);
        scanner.skip("rtt min/avg/max/mdev = ");
        String[] tmp = scanner.next().split("/| ");

        float minRttMs = Float.parseFloat(tmp[0]);
        float avgRttMs = Float.parseFloat(tmp[1]);
        float maxRttMs = Float.parseFloat(tmp[2]);
        float meanDeviation = Float.parseFloat(tmp[3]);

        return new PingResult(packetsSent, packetsSent - packetsReceived, minRttMs, avgRttMs, maxRttMs, meanDeviation, address);
    }
}
