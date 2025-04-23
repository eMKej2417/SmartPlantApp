package com.example.myapplication;

import android.os.Handler;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * This class handles fetching the latest moisture data from a Google Apps Script endpoint.
 */
public class DataFetcher {

    /**
     * Interface used to return data or an error back to the caller asynchronously.
     */
    public interface FetchCallback {
        void onResult(String timestamp, String moisture); // Called when data is successfully fetched
        void onError(String error); // Called when an error occurs
    }

    /**
     * Fetches the latest moisture reading from the specified URL.
     *
     * @param url The base URL of Google Apps Script endpoint
     * @param handler Handler used to post updates back to the UI thread
     * @param callback The callback to handle success or error
     */
    public static void fetchLatestData(String url, Handler handler, FetchCallback callback) {
        new Thread(() -> {
            try {
                // Adds timestamp to prevent cached response
                URL fullUrl = new URL(url + "?latest=1&timestamp=" + System.currentTimeMillis());

                // Set up HTTP GET request
                HttpURLConnection conn = (HttpURLConnection) fullUrl.openConnection();
                conn.setRequestMethod("GET");

                // Read the response
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) response.append(line);
                in.close();

                // Parse JSON result
                JSONObject data = new JSONObject(response.toString());
                String timestamp = data.getString("timestamp"); // e.g. "2025-04-18 12:00"
                String moisture = data.getString("moisture");   // e.g. "42"

                // Send result back to main thread using handler
                handler.post(() -> callback.onResult(timestamp, moisture));

            } catch (Exception e) {
                // Send error back to main thread
                handler.post(() -> callback.onError(e.getMessage()));
                Log.e("DataFetcher", "Error: " + e.getMessage()); // Log for debugging
            }
        }).start(); // Start thread immediately
    }
}
