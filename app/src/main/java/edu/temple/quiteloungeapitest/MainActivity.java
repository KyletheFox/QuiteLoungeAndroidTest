package edu.temple.quiteloungeapitest;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {



    private final static String GET_URL = "http://quietlounge.us-east-1.elasticbeanstalk.com/getLoungeData";
    private final static String POST_URL = "http://quietlounge.us-east-1.elasticbeanstalk.com/inputSound";
    private final static int TIME_BETWEEN_HTTP_REQUESTS = 2000;
    private double lat;
    private double lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        this.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Toast.makeText(getApplicationContext(), "Accuracy: " + location.getAccuracy(), Toast.LENGTH_SHORT).show();
                lat = location.getLatitude();
                lng = location.getLongitude();
                Log.d("Update", "Location Updated");
                Log.d("New Coordinates", "Lat: " + lat + " Lng: " + lng);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        final RequestQueue queue = Volley.newRequestQueue(this);

        // Create Timer to Constantly send new sound Data
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                queue.add(insertSoundData());
            }
        }, new Date(), TIME_BETWEEN_HTTP_REQUESTS);
    }

    @SuppressWarnings("unused")
    public JsonObjectRequest getLoungeData() {
        return new JsonObjectRequest(Request.Method.GET, GET_URL,
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray dataJson = response.getJSONArray("lounges");
                    for (int i = 0; i < dataJson.length(); i++) {
                        JSONObject obj = dataJson.getJSONObject(i);
                        System.out.println(obj.getString("name") + " - SoundLevel: " + obj.getDouble("lastSoundLevel"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
    }

    public JsonObjectRequest insertSoundData() {
        
        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, POST_URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String responseMsg = response.getString("msg");
                            System.out.println("Response Message: " + responseMsg);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        })
        {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            public byte[] getBody() {
                try {
                    String bodyStr;
                    bodyStr = "lat=" + lat + "&lng=" + lng + "&sound=" + String.valueOf(Math.random() * 30);
                    return bodyStr.getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };

//        Log.d("requestData", new String(request.getBody(), StandardCharsets.UTF_8));
//        Log.d("requestURL", request.getUrl());
//        Log.d("requestBodyType", request.getBodyContentType());
//        Log.d("request", request.toString());

        return request;
    }
}

