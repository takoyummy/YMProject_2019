package com.example.ds.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class SearchActivity extends AppCompatActivity implements SensorEventListener {
    private TextToSpeech textToSpeech;
    SpeechRecognizer mRecognizer;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextView tv;
    ProgressDialog progressDialog;
    String result;
    Boolean said = true;

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
        setContentView(R.layout.activity_search);

        //addRealtimeUpdate();
        progressDialog = new ProgressDialog(SearchActivity.this);
        progressDialog.show();
        progressDialog.setMessage("검색중입니다.");



        DocumentReference Searching3 = db.collection("yourmate").document("results");
        Searching3
                .update("result", " ")
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("statuss", "DocumentSnapshot successfully updated!");

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("statuss", "Error updating document", e);
                    }
                });

        tv= (TextView) findViewById(R.id.txtResult);
        tv.setMovementMethod(new ScrollingMovementMethod());


        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {

                    int result = textToSpeech.setLanguage(Locale.KOREA);

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(SearchActivity.this, "이 언어는 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        textToSpeech.setPitch(1.0f);

                        textToSpeech.setSpeechRate(1.0f);

                    }
                }
            }
        });
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerormeterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        addRealtimeUpdate2();





    }




/*
    private void addRealtimeUpdate() {
        DocumentReference contactListener = db.collection("R2").document("results");
        contactListener.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.d("ERROR", e.getMessage());
                    return;
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    //Toast.makeText(SearchActivity.this, "Current data:" + documentSnapshot.getData(), Toast.LENGTH_SHORT).show();
                    //result = documentSnapshot.getString("result");

                    documentSnapshot = null;


                }


            }
        });
    }
    */


    private void addRealtimeUpdate2() {

        if(said == true) {
            DocumentReference contactListener = db.collection("yourmate").document("results");
            contactListener.addSnapshotListener(new EventListener<DocumentSnapshot>() {


                @Override
                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.d("ERROR", e.getMessage());
                        return;


                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        //Toast.makeText(SearchActivity.this, "Current data:" + documentSnapshot.getData(), Toast.LENGTH_SHORT).show();


                        result = documentSnapshot.getString("result");
                        documentSnapshot = null;
                        said = false;
                        sayResult();



                    }


                }
            });
        }
    }



    public void sayResult(){


        if(said == false) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(result.contains(".")){
                        tv.setText(result);
                        textToSpeech.speak(tv.getText() + "쉐이킹하시면 메인 화면으로 돌아갑니다.", TextToSpeech.QUEUE_FLUSH, null, null);
                        progressDialog.dismiss();
                    }

                }
            }, 1000);

        }
    }



    private void stopListening()
    {
        if(mRecognizer != null)
        {
            mRecognizer.stopListening();
            mRecognizer.destroy();

        }
    }



    public void speakAgain(View view) {

        textToSpeech.speak(tv.getText(),TextToSpeech.QUEUE_FLUSH,null,null);
    }

    public void onStart() {
        super.onStart();
        if (accelerormeterSensor != null)
            sensorManager.registerListener(this, accelerormeterSensor,
                    SensorManager.SENSOR_DELAY_GAME);
    }


    protected void onStop() {
        super.onStop();
        textToSpeech.stop();
        textToSpeech.shutdown();
        if (sensorManager != null)
            sensorManager.unregisterListener(this);
        DocumentReference Searching = db.collection("yourmate").document("search");

        Searching
                .update("stat", "end")
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("statuss", "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("statuss", "Error updating document", e);
                    }
                });

    }

    public void goToFirst(){
        Intent intent = new Intent(this, SayActivity.class);
        startActivity(intent);
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
                    goToFirst();
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
