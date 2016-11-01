package edu.temple.quiteloungeapitest;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
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

import java.util.Date;
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
bgit
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
                queue.add(getLoungeData());
            }
        }, new Date(), 2000);


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
                        System.out.println(obj.getString("name"));
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
}
