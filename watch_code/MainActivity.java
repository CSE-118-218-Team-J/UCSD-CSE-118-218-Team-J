package com.example.watch_server;

import static com.google.android.gms.wearable.DataMap.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.widget.RelativeLayout;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.SensorEvent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;
import android.content.Context;
import android.view.View;

import android.os.Vibrator;

import android.view.WindowManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;



// class to handle sensor events
class MySensorEventListener implements SensorEventListener {
    // variable to access application's context
    private Context context;
    // variable to display text in the app
    private TextView hrtext;

    private RelativeLayout main_layout;

    public MySensorEventListener(Context context, TextView heartrate, RelativeLayout main_layout) {
        this.context = context;
        this.hrtext = heartrate;
        this.main_layout = main_layout;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // update TextView to say that sensor data has changed
        hrtext.setText(R.string.waiting);
        int heart_rate = 0;
        // if the sensor is a heart rate sensor, retrieve the heart rate data
        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            heart_rate = (int) event.values[0];
            Log.d("HR", "HERE3");
        }

        // if the heart rate surpasses a certain threshold, make the watch vibrate
        int threshold = 90;

        if (heart_rate > threshold) {
            // vibration + change of background
            vibrateWatch();
        } else {
            // when it's below the threshold, return the background to black
            main_layout.setBackgroundResource(R.color.black);
        }

        // display heart rate value
        if (heart_rate != 0) {
            String s = "" + heart_rate;
            hrtext.setText(s);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int a) {
        // not necessary for this program
    }

    // function to make the watch vibrate and change the background color
    private void vibrateWatch() {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.vibrate(500);
        }
        main_layout.setBackgroundResource(R.color.red);
    }
}

// custom AsyncTask class to perform background tasks (HTTP POST requests)
class keepPosting extends AsyncTask<Void, Void, Void> {
    public TextView hr;
    public String ID;

    public keepPosting(TextView HeartRate, String id){
        this.hr = HeartRate;
        this.ID = id;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        while(true){
            try {
                // logs to check the correct functioning of the program
                Log.d("MY", "HERE1"); //
                // create a URL object for the server endpoint
                URL url = new URL("https://bonefish-boss-singularly.ngrok-free.app/api/hr");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                Log.d("MY", "HERE3"); //

                /*watch -> server*/
                // set the request method to POST
                con.setRequestMethod("POST");
                Log.d("MY", "HERE4"); //
                // set the content type to indicate that you are sending JSON data
                con.setRequestProperty("Content-Type", "application/json");
                Log.d("MY", "HERE5"); //
                // enable input and output streams
                con.setDoInput(true);
                con.setDoOutput(true);
                con.setConnectTimeout(10000);
                con.setReadTimeout(10000);
//                Thread.sleep(5000);
                // create a JSON object with the heart rate value
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("hr", this.hr.getText().toString());
                jsonObject.put("ID", this.ID);
                Log.d("MY", "HERE6"); //
                //con.getOutputStream();
                Log.d("MY", "test");
                // write the JSON payload to the output stream
                try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                    // write the JSON payload to the output stream
                    Log.d("MY", "HERE7"); //
                    wr.writeBytes(String.valueOf(jsonObject));
                    wr.flush();
                }
                Log.d("MY", "HERE8"); //

                /*server -> watch*/
                // get the response code
                int responseCode = con.getResponseCode();
                Log.d(TAG, "Response Code: " + responseCode);
                Log.d("MY", "HERE9"); //

                // read the response from the server
                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }

                    // print the response
                    Log.d(TAG, "Response: " + response.toString());
                }
                // disconnect and wait for 5 seconds before the next iteration
                con.disconnect();
                Thread.sleep(5000);
            } catch (MalformedURLException | ProtocolException | InterruptedException |
                     JSONException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                Log.d("MY", "IOException");
                throw new RuntimeException(e);
            }
        }
    }
}
public class MainActivity extends AppCompatActivity {

    TextView textView1;
    TextView view_text;
    TextView view_number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // set layout for app
        setContentView(R.layout.activity_main);
        textView1 = findViewById(R.id.myTextView);

        // get main layout
        RelativeLayout main_layout = findViewById(R.id.main_layout);

        // keep the screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // get sensor service
        SensorManager mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // request sensor permission if not granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {
            textView1.setText(R.string.sensor_permission);
            Log.d("HR", "HERE1");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BODY_SENSORS}, 1);
        }
        String id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

        Log.d("HR", "HERE5");
        Log.d("MY", id);
        // create a sensor event listener
        //new MySensorEventListener(getBaseContext(), textView);
        view_text = findViewById(R.id.text_down);
        view_number = findViewById(R.id.text_up);
        SensorEventListener sensorListener = new MySensorEventListener(getBaseContext(), view_text, main_layout);
        Sensor mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

        mSensorManager.registerListener(sensorListener, mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
        // start an AsyncTask for posting data to the server
        new keepPosting(view_text, id).execute();
    }
}
