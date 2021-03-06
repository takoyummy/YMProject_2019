package com.example.ds.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class SaySearchActivity extends AppCompatActivity {


    private TextToSpeech textToSpeech;
    //Intent intent;
    SpeechRecognizer mRecognizer;
    //TextView textView;
    final int PERMISSION = 1;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_say_search);

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

        if (Build.VERSION.SDK_INT >= 23) {
            // ????????? ??????
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET,
                    Manifest.permission.RECORD_AUDIO}, PERMISSION);
        }
        pref = getSharedPreferences("mode", Activity.MODE_PRIVATE);

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {

                    int result = textToSpeech.setLanguage(Locale.KOREA);

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(SaySearchActivity.this, "??? ????????? ???????????? ????????????.", Toast.LENGTH_SHORT).show();
                    } else {
                        textToSpeech.setPitch(1.0f);

                        textToSpeech.setSpeechRate(1.0f);

                    }
                }
            }
        });
        //oncreate?????? ?????? ?????? ????????? ????????? ???????????? ?????? ????????? ??????.
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String sentence = "?????? ??????????????????.???????????? ?????? ??????????????? ???????????? ?????? ?????????????????? ?????? ????????? ??????????????????,?????????????????? ??????????????? ????????? ????????? ??????????????????.????????? ??? ????????? ??????????????? ?????? ????????? ??????????????? ??????????????????.????????? ????????? ?????????????????? ";
                textToSpeech.speak(sentence, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        }, 1900);


        //????????? ?????? ???????????? ?????? ??? ????????? ?????? ???????????? ??????????????? ?????????
        Handler delayHandler = new Handler();
        delayHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                createRecognizer();
            }
        }, 21000);


    }


    //?????????
    private RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {
            Toast.makeText(getApplicationContext(), "??????????????? ???????????????.", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float rmsdB) {
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
        }

        @Override
        public void onEndOfSpeech() {


        }

        @Override
        public void onError(int error) {
            String message;
            mRecognizer.stopListening();
            mRecognizer.destroy();
            Handler delayHandler = new Handler();
            String sentence = "?????? ??????????????????.";


            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = "????????? ??????";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    message = "??????????????? ??????";
                    textToSpeech.speak(sentence, TextToSpeech.QUEUE_FLUSH, null, null);


                    delayHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // TODO
                            createRecognizer();
                        }
                    }, 1500);

                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "????????? ??????";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = "???????????? ??????";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "????????? ????????????";
                    textToSpeech.speak(sentence, TextToSpeech.QUEUE_FLUSH, null, null);


                    delayHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // TODO
                            createRecognizer();
                        }
                    }, 1500);

                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = "?????? ??? ??????";
                    textToSpeech.speak(sentence, TextToSpeech.QUEUE_FLUSH, null, null);


                    delayHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // TODO
                            createRecognizer();
                        }
                    }, 1500);

                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "RECOGNIZER??? ??????";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "????????? ?????????";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "????????? ????????????";
                    textToSpeech.speak(sentence, TextToSpeech.QUEUE_FLUSH, null, null);


                    delayHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // TODO
                            createRecognizer();
                        }
                    }, 1500);

                    break;

                default:
                    message = "??? ??? ?????? ?????????";
                    textToSpeech.speak(sentence, TextToSpeech.QUEUE_FLUSH, null, null);


                    delayHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // TODO
                            createRecognizer();
                        }
                    }, 1500);

                    break;
            }

            //Toast.makeText(getApplicationContext(), "????????? ?????????????????????. : " + message,Toast.LENGTH_SHORT).show();
        }

        //????????? result??? ???????????? ?????? ??????????????? ?????????
        @Override
        public void onResults(Bundle results) {
            // ?????? ?????? ArrayList??? ????????? ?????? textView??? ????????? ???????????????.
            stopListening();
            ArrayList<String> matches =
                    results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            //testMatches(matches);
            String keywords = matches.get(0);

            DocumentReference Searching = db.collection("yourmate").document("search");
            DocumentReference Searching2 = db.collection("yourmate").document("keywords");



            Searching
                    .update("stat", "start")
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

            Searching2
                    .update("keyword", keywords)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("statuss", "DocumentSnapshot successfully updated!");
                            //addRealtimeUpdate2();
                            goToSearch();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("statuss", "Error updating document", e);
                        }
                    });


        }

        @Override
        public void onPartialResults(Bundle partialResults) {
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
        }
    };

    public void goToSearch() {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
    }


    //??????????????? ?????? ?????? ?????? array?????? ???????????? ?????????????????? ???????????? ?????? ???????????? camera???????????????, ????????? ????????? ?????? ?????????????????? ?????????
    public void testMatches(ArrayList<String> matches) {
        for (int i = 0; i < matches.size(); i++) {


            if (matches.get(i).equals("?????? ??????")) {

                textToSpeech.stop();
                textToSpeech.shutdown();
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("modes", 1);
                editor.commit();
                finish();


            } else if (matches.get(i).equals("??????") || matches.get(i).equals("?????? ?????? ???") || matches.get(i).equals("?????? ?????? ??????") || matches.get(i).equals("?????? ?????? ???")) {

                textToSpeech.stop();
                textToSpeech.shutdown();
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("modes", 2);
                editor.commit();
                finish();


            } else {
                stopListening();

                String sentence = "?????? ??????????????????.";
                textToSpeech.speak(sentence, TextToSpeech.QUEUE_FLUSH, null, null);
                Handler delayHandler = new Handler();
                delayHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // TODO
                        createRecognizer();
                    }
                }, 2000);


            }

        }
    }


    //text to speech stop ????????? ?????????

    public void createRecognizer() {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "com.example.ds.myapplication");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");

        mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mRecognizer.setRecognitionListener(listener);

        mRecognizer.startListening(intent);

    }

    //???????????? ?????? ??????
    private void stopListening() {
        if (mRecognizer != null) {
            mRecognizer.stopListening();
            mRecognizer.destroy();

        }
    }


    @Override
    protected void onStop() {
        super.onStop();

        textToSpeech.stop();
        textToSpeech.shutdown();
        stopListening();
        finish();
    }


}
