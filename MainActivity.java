package com.example.lab5;

import static androidx.core.content.ContextCompat.getSystemService;
import static com.google.android.gms.wearable.DataMap.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.SensorEvent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

class MySensorEventListener implements SensorEventListener {
    private Context context;
    private TextView textView;

    public MySensorEventListener(Context context, TextView textView) {
        this.context = context;
        this.textView = textView;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        textView.setText(R.string.changesensor);
        int heart_rate = 0;
        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            heart_rate = (int)event.values[0];
        }
        String s = "heart rate: "+heart_rate;
        textView.setText(s);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int a) {
    }
}

class keepPosting extends AsyncTask<Void, Void, Void> {
    public String hr;

    public keepPosting(int HeartRate){
        this.hr = Integer.toString(HeartRate);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        while(true){
            try {
                Log.d("MY", "HERE1");
                URL url = new URL("https://bonefish-boss-singularly.ngrok-free.app/");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                Log.d("MY", "HERE3");
                // Set the request method to POST
                con.setRequestMethod("POST");
                Log.d("MY", "HERE4");
                // Set the content type to indicate that you are sending JSON data
                con.setRequestProperty("Content-Type", "application/json");
                Log.d("MY", "HERE5");
                // Enable input and output streams
                con.setDoOutput(true);
                String jsonInputString = "{\"hr\": \"{this.hr}\" }";
                Log.d("MY", "HERE6");
                try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                    // Write the JSON payload to the output stream
                    Log.d("MY", "HERE7");
                    wr.writeBytes(jsonInputString);
                    wr.flush();
                }
                Log.d("MY", "HERE8");
                // Get the response code
                int responseCode = con.getResponseCode();
                Log.d(TAG, "Response Code: " + responseCode);

                // Read the response from the server
                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }

                    // Print the response
                    Log.d(TAG, "Response: " + response.toString());
                }

                // Close the connection
                con.disconnect();

                // Sleep for the specified delay before the next iteration
                Log.d("MY", "HERE2");
                Thread.sleep(5000);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            } catch (ProtocolException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                Log.d("MY", "IOException");
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
public class MainActivity extends AppCompatActivity {

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.myTextView);
        new keepPosting(50).execute();
//        String url = "http://bonefish-boss-singularly.ngrok-free.app/";
//        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, response -> {});
//        SensorManager mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {
//            textView.setText(R.string.sensor_permission);
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.BODY_SENSORS}, 1);
//        }
//
//        String wait_sensor = "Waiting for data from sensor: " + SensorManager.SENSOR_STATUS_UNRELIABLE;
//        textView.setText(wait_sensor);
//
//        SensorEventListener sensorListener = new MySensorEventListener(getBaseContext(), textView);
//        Sensor mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
//
//        mSensorManager.registerListener(sensorListener, mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
}

/*
public class MainActivity extends AppCompatActivity{
    SensorManager mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

    mSensorManager.getS


}
*/

