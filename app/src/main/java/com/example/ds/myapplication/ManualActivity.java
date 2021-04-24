package com.example.ds.myapplication;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Toast;

import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;

public class ManualActivity extends AppCompatActivity implements SensorEventListener {

    private TextToSpeech textToSpeech;

    private long lastTime;
    private float speed;
    private float lastX;
    private float lastY;
    private float lastZ;
    private float x, y, z;

    private static final int SHAKE_THRESHOLD = 5000;
    private static final int DATA_X = SensorManager.DATA_X;
    private static final int DATA_Y = SensorManager.DATA_Y;
    private static final int DATA_Z = SensorManager.DATA_Z;

    private SensorManager sensorManager;
    private Sensor accelerormeterSensor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerormeterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {

                    int result = textToSpeech.setLanguage(Locale.KOREA);

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(ManualActivity.this, "이 언어는 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        textToSpeech.setPitch(1.0f);

                        textToSpeech.setSpeechRate(1.0f);

                    }
                }
            }
        });



        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {


                String sentence = "Your Mate는 시력 기능이 저하된 분들을 위한 음성 서비스입니다. 메인화면의 하늘색 버튼을 누르시면 카메라를 통해 주변 상황을 탐색하고 글자를 인식하는등 이미지를 분석해드리고, 최하단의 곤색 버튼을 누르시면 단어 검색 및 실시간 순위와 헤드라인을 알려드리는 검색 서비스를 제공해드립니다. 모든 서비스는 음성으로 제공됩니다. 쉐이킹하시거나 하단의 홈 버튼을 누르시면 메인 화면으로 돌아갑니다. ";
                textToSpeech.speak(sentence, TextToSpeech.QUEUE_FLUSH, null,null);



            }
        }, 1000);




    }

    protected void onStop() {
        super.onStop();

        if (sensorManager != null)
            sensorManager.unregisterListener(this);

        textToSpeech.stop();
        textToSpeech.shutdown();
    }

    public void goToMain(View view) {


        textToSpeech.stop();
        textToSpeech.shutdown();
        finish();

        Intent intent = new Intent(this, SayActivity.class);
        startActivity(intent);


    }

    public void onStart() {
        super.onStart();
        if (accelerormeterSensor != null)
            sensorManager.registerListener(this, accelerormeterSensor,
                    SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long currentTime = System.currentTimeMillis();
            long gabOfTime = (currentTime - lastTime);
            if (gabOfTime > 100) {
                lastTime = currentTime;
                x = event.values[SensorManager.DATA_X];
                y = event.values[SensorManager.DATA_Y];
                z = event.values[SensorManager.DATA_Z];

                speed = Math.abs(x + y + z - lastX - lastY - lastZ) / gabOfTime * 10000;

                if (speed > SHAKE_THRESHOLD) {

// 이벤트발생!!
                    textToSpeech.stop();
                    textToSpeech.shutdown();
                    finish();

                    Intent intent = new Intent(this, SayActivity.class);
                    startActivity(intent);
                }

                lastX = event.values[DATA_X];
                lastY = event.values[DATA_Y];
                lastZ = event.values[DATA_Z];
            }

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
