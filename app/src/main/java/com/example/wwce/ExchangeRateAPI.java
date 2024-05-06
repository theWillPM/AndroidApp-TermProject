package com.example.wwce;

import android.os.AsyncTask;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Exchange Rate API class - code mainly from their website <a href="https://www.exchangerate-api.com/docs/java-currency-api">...</a>
 */
public class ExchangeRateAPI extends AsyncTask<Void, Void, JsonObject> {

    private String apiUrl;
    private OnExchangeRateListener listener;

    public ExchangeRateAPI(String apiUrl, OnExchangeRateListener listener) {
        this.apiUrl = apiUrl;
        this.listener = listener;
    }

    // I couldn't change doInBackground to on concurrent (tried after I had already built the app)
    // Code for the API is available on their website at https://www.exchangerate-api.com/docs/java-currency-api
    @Override
    protected JsonObject doInBackground(Void... voids) {
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(apiUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = urlConnection.getInputStream();
            InputStreamReader reader = new InputStreamReader(in);

            // Parse JSON response
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();

            // Log JSON response
            Log.d("ExchangeRateAsyncTask", "JSON Response: " + jsonObject.toString());

            return jsonObject;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return null;
    }


    // needed to broadcast event
    @Override
    protected void onPostExecute(JsonObject jsonObject) {
        super.onPostExecute(jsonObject);
        if (listener != null) {
            listener.onExchangeRateReceived(jsonObject);
        }
    }

    public interface OnExchangeRateListener {
        void onExchangeRateReceived(JsonObject jsonObject);
    }
}
