package com.example.ds.myapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.ml.vision.text.RecognizedLanguage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import edmt.dev.edmtdevcognitivevision.Contract.AnalysisResult;
import edmt.dev.edmtdevcognitivevision.Contract.Caption;
import edmt.dev.edmtdevcognitivevision.Rest.VisionServiceException;
import edmt.dev.edmtdevcognitivevision.VisionServiceClient;
import edmt.dev.edmtdevcognitivevision.VisionServiceRestClient;

//import android.support.v7.app.AppCompatActivity;

//?????? ????????????
public class ResultActivity extends AppCompatActivity implements SensorEventListener {


    private TextToSpeech textToSpeech;
    private TextToSpeech tts;
    TextView tv;
    Bitmap bm = null;
    ProgressDialog progressDialog;
    SharedPreferences pref;

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


    String eng_result;

    public ResultActivity() {
    }

    private final String API_KEY = "????????? ???";
    private final String API_LINK = "????????? ??????";

    VisionServiceClient visionServiceClient = new VisionServiceRestClient(API_KEY, API_LINK);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_result);
        progressDialog = new ProgressDialog(ResultActivity.this);
        //????????? ????????????????????? ???????????? ?????? ????????? ???????????? ??????
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    //????????? ????????? ??????
                    int result = textToSpeech.setLanguage(Locale.KOREA);
                    //?????? ???????????? ????????? ?????? ????????? ???????????? ?????????...
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(ResultActivity.this, "??? ????????? ???????????? ????????????.", Toast.LENGTH_SHORT).show();
                    } else {


                    }
                }
            }
        });
        ImageView iv = (ImageView) findViewById(R.id.imgResult);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerormeterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //?????? ???????????? ????????? ?????? ????????? uri ??? ?????????
        Intent intent = getIntent();
        Bundle extra = getIntent().getExtras();
        Uri uri = intent.getParcelableExtra("uri");


        //uri ??? ???????????? set image ????????? ???.
        try {
            bm = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            iv.setImageBitmap(bm);
        } catch (IOException e) {
            e.printStackTrace();
        }

        tv = (TextView) findViewById(R.id.txtResult);
        tv.setMovementMethod(new ScrollingMovementMethod());
        pref = getSharedPreferences("mode", Activity.MODE_PRIVATE);
        int result_mode = pref.getInt("modes", 0);


        if (result_mode == 1) {
            analyzeImage();
        } else {
            analyzeText();
        }


    }

    //????????? ?????? ????????? ?????? ????????? ?????????
    public void speakAgain(View view) {
        //String sentence = (String) tv.getText();
        textToSpeech.speak(tv.getText(), TextToSpeech.QUEUE_FLUSH, null, null);

    }

    //????????? ?????? ???????????? ????????? ?????? ?????????????????? ?????????
    public void cameraAgain(View view) {
        Intent intent = new Intent(this, CameraActivity.class);
        textToSpeech.stop();
        textToSpeech.shutdown();
        finish();
        startActivity(intent);

    }

    //????????? ?????? ???????????????
    public void analyzeImage() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        AsyncTask<InputStream, String, String> visionTask = new AsyncTask<InputStream, String, String>() {


            @Override
            protected void onPreExecute() {
                progressDialog.show();
            }

            @Override
            protected String doInBackground(InputStream... inputStreams) {
                try {
                    publishProgress("?????????...");
                    String[] features = {"Description"};
                    String[] details = {};

                    AnalysisResult result = visionServiceClient.analyzeImage(inputStreams[0], features, details);

                    String jsonResult = new Gson().toJson(result);
                    return jsonResult;
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (VisionServiceException e) {
                    e.printStackTrace();
                }
                return "";
            }

            @Override
            protected void onPostExecute(String s) {

                if (TextUtils.isEmpty(s)) {

                    Toast.makeText(ResultActivity.this, "API Return Empty Result", Toast.LENGTH_SHORT).show();
                } else {

                    AnalysisResult result = new Gson().fromJson(s, AnalysisResult.class);
                    StringBuilder result_Text = new StringBuilder();
                    for (Caption caption : result.description.captions)
                        result_Text.append(caption.text);
                    eng_result = result_Text.toString();
                    translate();


                }

            }

            @Override
            protected void onProgressUpdate(String... values) {
                progressDialog.setMessage(values[0]);

            }

        };
        visionTask.execute(inputStream);
    }

    public void translate() {
        NaverTranslateTask asyncTask = new NaverTranslateTask();
        asyncTask.execute(eng_result);
    }

    public void goToFirst() {
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

                    // ???????????????!!
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

    //??????????????? ?????????
    public class NaverTranslateTask extends AsyncTask<String, Void, String> {

        public String resultText;
        //Naver
        String clientId = "????????? ????????????????????????";//?????????????????? ??????????????? ????????????";
        String clientSecret = "????????? ????????????";//?????????????????? ??????????????? ????????????";
        //??????????????? ????????? ???????????? ????????? ??? ?????? ?????? ????????? ?????? ??????.
        String sourceLang = "en";
        String targetLang = "ko";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected String doInBackground(String... strings) {


            String sourceText = strings[0];

            try {

                String text = URLEncoder.encode(sourceText, "UTF-8");
                String apiURL = "https://openapi.naver.com/v1/papago/n2mt";
                URL url = new URL(apiURL);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("X-Naver-Client-Id", clientId);
                con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
                // post request
                String postParams = "source=" + sourceLang + "&target=" + targetLang + "&text=" + text;
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(postParams);
                wr.flush();
                wr.close();
                int responseCode = con.getResponseCode();
                BufferedReader br;
                if (responseCode == 200) { // ?????? ??????
                    br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                } else {  // ?????? ??????
                    br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                }
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }
                br.close();

                return response.toString();

            } catch (Exception e) {

                Log.d("error", e.getMessage());
                return null;
            }
        }

        //????????? ????????? ????????? ??????
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //?????? ?????? ?????????
            //Log.d("background result", s.toString());

            //JSON???????????? ??????????????? ???????????? ??????.
            //Gson??? ????????? ?????????.
            progressDialog.dismiss();
            Gson gson = new GsonBuilder().create();
            JsonParser parser = new JsonParser();
            JsonElement rootObj = parser.parse(s.toString())
                    //????????? ????????? ?????? ?????? ????????????.
                    .getAsJsonObject().get("message")
                    .getAsJsonObject().get("result");
            //??????????????? ????????? ??????
            TranslatedItem items = gson.fromJson(rootObj.toString(), TranslatedItem.class);

            //??????????????? ??????????????? ?????????.

            String kor_result = items.getTranslatedText();

            tv.setText("????????? " + kor_result + " ?????????.");

            final Handler handler = new Handler();

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Do something after 100ms

                    textToSpeech.speak("????????? " + kor_result + " ?????????. ?????? ????????? ??????????????? ???????????? ?????? ???????????????. ?????????????????? ?????? ???????????? ???????????????", TextToSpeech.QUEUE_FLUSH, null, null);

                    //mRecognizer.startListening(intent);

                }
            }, 300);

        }


        //????????? ??????
        private class TranslatedItem {
            String translatedText;

            public String getTranslatedText() {
                return translatedText;
            }
        }
    }

    //??????????????? text??????
    public void analyzeText() {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bm);
        progressDialog.setMessage("?????????...");
        progressDialog.show();
        recognizeTextCloud(image);

    }


    private void recognizeTextCloud(FirebaseVisionImage image) {

        FirebaseVisionCloudTextRecognizerOptions options = new FirebaseVisionCloudTextRecognizerOptions.Builder()
                .setLanguageHints(Arrays.asList("ko", "hi"))
                .build();

        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                .getCloudTextRecognizer();

        Task<FirebaseVisionText> result = detector.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText result) {

                        String resultText = result.getText();
                        for (FirebaseVisionText.TextBlock block : result.getTextBlocks()) {
                            String blockText = block.getText();
                            Float blockConfidence = block.getConfidence();
                            List<RecognizedLanguage> blockLanguages = block.getRecognizedLanguages();
                            Point[] blockCornerPoints = block.getCornerPoints();
                            Rect blockFrame = block.getBoundingBox();
                            for (FirebaseVisionText.Line line : block.getLines()) {
                                String lineText = line.getText();
                                Float lineConfidence = line.getConfidence();
                                List<RecognizedLanguage> lineLanguages = line.getRecognizedLanguages();
                                Point[] lineCornerPoints = line.getCornerPoints();
                                Rect lineFrame = line.getBoundingBox();
                                for (FirebaseVisionText.Element element : line.getElements()) {
                                    String elementText = element.getText();
                                    Float elementConfidence = element.getConfidence();
                                    List<RecognizedLanguage> elementLanguages = element.getRecognizedLanguages();
                                    Point[] elementCornerPoints = element.getCornerPoints();
                                    Rect elementFrame = element.getBoundingBox();
                                }
                            }
                        }
                        progressDialog.dismiss();
                        tv.setText(resultText);
                        final Handler handler = new Handler();

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {


                                textToSpeech.speak(resultText + "?????? ????????? ??????????????? ???????????? ?????? ???????????????. ?????????????????? ?????? ???????????? ???????????????", TextToSpeech.QUEUE_FLUSH, null, null);


                            }
                        }, 1000);


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        tv.setText("???????????? ?????? ??? ????????????.");
                        progressDialog.dismiss();
                    }
                });

        
    }

    public void onStart() {
        super.onStart();
        if (accelerormeterSensor != null)
            sensorManager.registerListener(this, accelerormeterSensor,
                    SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onStop() {
        super.onStop();
        textToSpeech.stop();
        textToSpeech.shutdown();
        if (sensorManager != null)
            sensorManager.unregisterListener(this);

    }


}
