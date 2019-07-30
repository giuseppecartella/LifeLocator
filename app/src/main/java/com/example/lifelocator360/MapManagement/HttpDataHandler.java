package com.example.lifelocator360.MapManagement;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class HttpDataHandler {
    private URL url;
    private String response;
    private HttpURLConnection httpURLConnection;
    private int responseCode;
    private String line;
    private BufferedReader bufferedReader;


    public HttpDataHandler() {

    }

    public String getHTTPData(String requestURL) {
        response = "";

        try {
            url = new URL(requestURL);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setReadTimeout(15000);
            httpURLConnection.setConnectTimeout(15000);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            responseCode = httpURLConnection.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                while ((line = bufferedReader.readLine()) != null) {
                    response += line;
                }
            } else {
                response = "";
            }

        } catch (ProtocolException e) {
            Log.d("ERRORE", "errore 1");
            e.printStackTrace();
        } catch (MalformedURLException e) {
            Log.d("ERRORE", "errore 2");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d("ERRORE", "errore 3");
            e.printStackTrace();
        }
        return response;
    }
}
