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


public class NativePingCommand implements IPingCommand {
    private static final int REQUEST_NUMBER = 5;

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


    private String checkErrors(InputStream in) throws IOException {
        BufferedReader inReader = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();

        String s;
        while ((s = inReader.readLine()) != null) {
            sb.append(s);
        }

        if(sb.length() == 0)
            return null;

        return sb.toString();
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

    public IPingResult execute(InetAddress address) throws InterruptedException, IOException {
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
                setIsRunning(false);
                throw e;
            }
        }

        InputStream in = _process.getInputStream();
//        OutputStream out = _process.getOutputStream();
        InputStream err = _process.getErrorStream();

        String res;
        if((res = checkErrors(err)) != null) {
            throw new IOException("Ping Error: " + res);
        }
        Log.i("Ping Command", "Read: \"" + res + "\"");

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
        Log.i("SCANNER", "Scanning: " + rows);
        Log.i("SCANNER", "Scanning: \"" + rows[rows.length - 2] + "\"");
        Scanner scanner = new Scanner(rows[rows.length - 2]);
        int packetsSent = scanner.nextInt();
        scanner.skip(" packets transmitted, ");
        int packetsReceived = scanner.nextInt();

        Log.i("SCANNER", "Scanning: \"" + rows[rows.length - 1] + "\"");
        String rttRow = rows[rows.length - 1];


        float minRttMs;
        float avgRttMs;
        float maxRttMs;
        float meanDeviation;

        if(rttRow.equals("")) {
            minRttMs = 0;
            avgRttMs = 0;
            maxRttMs = 0;
            meanDeviation = 0;
        } else {
            scanner = new Scanner(rttRow);
            scanner.useLocale(Locale.ENGLISH);
            scanner.skip("rtt min/avg/max/mdev = ");
            String[] tmp = scanner.next().split("/| ");

            minRttMs = Float.parseFloat(tmp[0]);
            avgRttMs = Float.parseFloat(tmp[1]);
            maxRttMs = Float.parseFloat(tmp[2]);
            meanDeviation = Float.parseFloat(tmp[3]);
        }

        StringBuilder sb = new StringBuilder();
        for(String row : rows) {
            sb.append(row + '\n');
        }

        return new NativePingResult(packetsSent, packetsSent - packetsReceived, minRttMs, avgRttMs, maxRttMs, meanDeviation, address, sb.toString());
    }
}
