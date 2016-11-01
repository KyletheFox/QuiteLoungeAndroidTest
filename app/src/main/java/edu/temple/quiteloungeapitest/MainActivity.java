package edu.temple.quiteloungeapitest;

import android.app.Activity;
import android.os.Bundle;

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
    int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        final RequestQueue queue = Volley.newRequestQueue(this);

        // Create Timer to Constantly send new sound Data
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                counter++;
                System.out.println("Timer Went off :: " + counter);
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
