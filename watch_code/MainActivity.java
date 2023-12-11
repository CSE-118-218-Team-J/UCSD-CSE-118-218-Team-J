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
    private TextView hrText;

    private RelativeLayout mainLayout;
    // variable to create the vibrator
    private Vibrator vibrator;

    public MySensorEventListener(Context context, TextView heartrate, RelativeLayout mainLayout) {
        this.context = context;
        this.hrText = heartrate;
        this.mainLayout = mainLayout;
        this.vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // update TextView to say that sensor data has changed
        hrText.setText(R.string.waiting);
        int heartRate = 0;
        // if the sensor is a heart rate sensor, retrieve the heart rate data
        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            heartRate = (int) event.values[0];

        }
        Log.d("HR", "HERE " + Variables.threshold);
        // if the heart rate surpasses a certain threshold, make the watch vibrate
        if (heartRate > Variables.threshold) {
            // vibration + change of background
            vibrateWatch();
        } else {
            // when it's below the threshold, return the background to black
            mainLayout.setBackgroundResource(R.color.black);
        }

        // display heart rate value
        if (heartRate != 0) {
            String s = "" + heartRate;
            hrText.setText(s);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int a) {
        // not necessary for this program
    }

    // function to make the watch vibrate and change the background color
    private void vibrateWatch() {
        if (vibrator != null) {
            vibrator.vibrate(500);
        }
        mainLayout.setBackgroundResource(R.color.red);
    }

}

// custom AsyncTask class to perform background tasks (HTTP POST requests)
class serverConnection extends AsyncTask<Void, Void, Void> {
    public TextView hr;
    public String ID;
    public String thresh;

    public serverConnection(TextView HeartRate, String id){
        this.hr = HeartRate;
        this.ID = id;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        while(!Variables.stopCondition){
            try {
                URL url = new URL("https://bonefish-boss-singularly.ngrok-free.app/api/hr");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestProperty("Content-Type", "application/json");
                con.setDoInput(true);
                con.setDoOutput(true);
                con.setConnectTimeout(10000);
                con.setReadTimeout(10000);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("hr", this.hr.getText().toString());
                jsonObject.put("ID", this.ID);

                try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                    // write the JSON payload to the output stream
                    Log.d("MY", "HERE7"); //
                    wr.writeBytes(String.valueOf(jsonObject));
                    wr.flush();
                }

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
                Thread.sleep(1000);
                Log.d("HR", "HERE IN askThreshold");
                URL geturl = null;
                try {
                    geturl = new URL("https://bonefish-boss-singularly.ngrok-free.app/getThreshold");
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
                HttpURLConnection newcon = null;
                try {
                    newcon = (HttpURLConnection) geturl.openConnection();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                try {
                    newcon.setRequestMethod("GET");
                } catch (ProtocolException e) {
                    throw new RuntimeException(e);
                }
                newcon.setReadTimeout(1000);
                newcon.setConnectTimeout(5000);
                newcon.setDoOutput(true);
                try {
                    newcon.connect();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                BufferedReader br = null;
                try {
                    br = new BufferedReader(new InputStreamReader(geturl.openStream()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                StringBuilder sb = new StringBuilder();

                String str;
                while (true) {
                    try {
                        if (!((str = br.readLine()) != null)) break;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    sb.append(str + "\n");
                }
                try {
                    br.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                String jsonString = sb.toString();
                Log.d("MY", "JSON " + jsonString);

                JSONObject jobj = null;
                try {
                    jobj = new JSONObject((jsonString));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                try {
                    this.thresh = (jobj.get("threshold").toString());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                newcon.disconnect();
                Log.d("HR", "HERE IN GET REQUEST " + this.thresh);
                Variables.threshold = Integer.parseInt(this.thresh);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } catch (MalformedURLException | ProtocolException | InterruptedException |
                     JSONException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                Log.d("MY", "IOException");
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}
public class MainActivity extends AppCompatActivity {

    TextView textView1;
    TextView viewText;
    TextView viewNumber;

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
        viewText = findViewById(R.id.text_down);
        viewNumber = findViewById(R.id.text_up);
        SensorEventListener sensorListener = new MySensorEventListener(getBaseContext(), viewText, main_layout);
        Sensor mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

        mSensorManager.registerListener(sensorListener, mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
        // start an AsyncTask for posting data to the server
        new serverConnection(viewText, id).execute();
        Log.d("1", "BEFORE askThreshold");
    }

    @Override
    protected void onPause(){
        super.onPause();
        Variables.stopCondition =true;
    }

    @Override
    protected void onResume(){
        super.onResume();
        // reset the condition for the server loops
        Variables.stopCondition =false;
        String id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        new serverConnection(viewText, id).execute();
    }
}
