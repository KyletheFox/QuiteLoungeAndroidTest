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
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {

    private final static String url = "http://quietlounge.us-east-1.elasticbeanstalk.com/getLoungeData";
    double lat;
    double lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        //System.out.println(this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION));

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Toast.makeText(getApplicationContext(), "Accuracy: " + location.getAccuracy(), Toast.LENGTH_SHORT).show();
                lat = location.getLatitude();
                lng = location.getLongitude();
                System.out.println("Location Updated");
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
                System.out.println("Lat: " + lat + " Lng: " + lng);
                queue.add(insertSoundData());
            }
        }, new Date(), 2000);

//        System.out.println(insertSoundData());


//        queue.add(insertSoundData());
//        queue.add(getLoungeData());

    }

    public JsonObjectRequest getLoungeData() {
        return new JsonObjectRequest(Request.Method.GET, url,
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
        String postUrl = "http://quietlounge.us-east-1.elasticbeanstalk.com/inputSound";

        Map<String, String> params = new HashMap<String, String>();
        params.put("lat", String.valueOf(lat).trim()); //Add the data you'd like to send to the server.
        params.put("lng", String.valueOf(lng).trim()); //Add the data you'd like to send to the server.
        params.put("sound", String.valueOf(Math.random() * 30).trim()); //Add the data you'd like to send to the server.

        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, postUrl, new JSONObject(params),
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

            //            @Override
//            protected Map<String, String> getParams() {
//                Map<String, String> params = new HashMap<String, String>();
//                params.put("lat", String.valueOf(lat).trim()); //Add the data you'd like to send to the server.
//                params.put("lng", String.valueOf(lng).trim()); //Add the data you'd like to send to the server.
//                params.put("sound", String.valueOf(Math.random() * 30).trim()); //Add the data you'd like to send to the server.
//                return params;
//            }

//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String,String> headers = new HashMap<String, String>();
//                headers.put("Content-Type","application/x-www-form-urlencoded");
//                return headers;
//            }


        };

        Log.d("requestData", new String(request.getBody(), StandardCharsets.UTF_8));
        Log.d("requestURL", request.getUrl());
        Log.d("requestBodyType", request.getBodyContentType());
        Log.d("request", request.toString());



        return request;
    }
}

