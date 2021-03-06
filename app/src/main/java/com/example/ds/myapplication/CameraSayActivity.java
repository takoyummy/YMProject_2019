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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class CameraSayActivity extends AppCompatActivity {

    private TextToSpeech textToSpeech;
    //Intent intent;
    SpeechRecognizer mRecognizer;
    //TextView textView;
    final int PERMISSION = 1;
    SharedPreferences pref ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_camera_say);

        if ( Build.VERSION.SDK_INT >= 23 ){
            // 퍼미션 체크
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET,
                    Manifest.permission.RECORD_AUDIO},PERMISSION);
        }
        pref = getSharedPreferences("mode", Activity.MODE_PRIVATE);

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {

                    int result = textToSpeech.setLanguage(Locale.KOREA);

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(CameraSayActivity.this, "이 언어는 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        textToSpeech.setPitch(1.0f);

                        textToSpeech.setSpeechRate(1.0f);

                    }
                }
            }
        });
        //oncreate에서 바로 동작 안해서 일부러 딜레이를 주는 핸들러 이용.
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {


                String sentence = "이미지를 분석해드리는 서비스입니다.'이게 뭐야'라고 말씀하시면 전방의 상황을 알려드리고,'글자 읽어 줘'라고 말씀하시면 글자를 읽어드립니다.소리가 들리면 말씀해주세요 ";
                textToSpeech.speak(sentence, TextToSpeech.QUEUE_FLUSH, null,null);




            }
        }, 1900);


        //말하는 동안 딜레이를 주고 삐 소리가 나면 음성인식 실행시키는 메소드
        Handler delayHandler = new Handler();
        delayHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // TODO
                createRecognizer();
            }
        }, 16000);


    }


    //리스너
    private RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {
            Toast.makeText(getApplicationContext(),"음성인식을 시작합니다.",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float rmsdB) {}

        @Override
        public void onBufferReceived(byte[] buffer) {}

        @Override
        public void onEndOfSpeech() {


        }

        @Override
        public void onError(int error) {
            String message;
            mRecognizer.stopListening();
            mRecognizer.destroy();
            Handler delayHandler = new Handler();
            String sentence = "'이게 뭐야' 혹은 '글자 읽어줘'라고 말씀해주세요.";



            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = "오디오 에러";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    message = "클라이언트 에러";
                    textToSpeech.speak(sentence, TextToSpeech.QUEUE_FLUSH, null,null);


                    delayHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // TODO
                            createRecognizer();
                        }
                    }, 4500);

                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "퍼미션 없음";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = "네트워크 에러";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "네트웍 타임아웃";
                    textToSpeech.speak(sentence, TextToSpeech.QUEUE_FLUSH, null,null);


                    delayHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // TODO
                            createRecognizer();
                        }
                    }, 4500);

                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = "찾을 수 없음";
                    textToSpeech.speak(sentence, TextToSpeech.QUEUE_FLUSH, null,null);


                    delayHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // TODO
                            createRecognizer();
                        }
                    }, 4500);

                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "RECOGNIZER가 바쁨";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "서버가 이상함";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "말하는 시간초과";
                    textToSpeech.speak(sentence, TextToSpeech.QUEUE_FLUSH, null,null);


                    delayHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // TODO
                            createRecognizer();
                        }
                    }, 4500);

                    break;

                default:
                    message = "알 수 없는 오류임";
                    textToSpeech.speak(sentence, TextToSpeech.QUEUE_FLUSH, null,null);


                    delayHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // TODO
                            createRecognizer();
                        }
                    }, 4500);

                    break;
            }

            //Toast.makeText(getApplicationContext(), "에러가 발생하였습니다. : " + message,Toast.LENGTH_SHORT).show();
        }

        //여기가 result값 받아와서 다른 처리해주는 메소드
        @Override
        public void onResults(Bundle results) {
            // 말을 하면 ArrayList에 단어를 넣고 textView에 단어를 이어줍니다.
            stopListening();
            ArrayList<String> matches =
                    results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            testMatches(matches);


        }

        @Override
        public void onPartialResults(Bundle partialResults) {}

        @Override
        public void onEvent(int eventType, Bundle params) {}
    };







    //사용자에게 음성 입력 받은 array값을 가져와서 음성명령값과 비교하여 값이 똑같으면 camera액티비티를, 그렇지 않다면 다시 음성요청하는 매소드
    public void testMatches(ArrayList<String> matches){
        for(int i = 0; i < matches.size() ; i++){


            if(matches.get(i).equals("이게 뭐야")){

                textToSpeech.stop();
                textToSpeech.shutdown();
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("modes", 1);
                editor.commit();
                finish();

                goToCamera();

            }
            else if(matches.get(i).equals("글자")||matches.get(i).equals("글자 읽어 줘")||matches.get(i).equals("글자 읽어 줄래")||matches.get(i).equals("글짜 읽어 줘")){

                textToSpeech.stop();
                textToSpeech.shutdown();
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("modes", 2);
                editor.commit();
                finish();

                goToCamera();

            }


            else {
                stopListening();

                String sentence = "'이게 뭐야' 혹은 '글자 읽어줘'라고 말씀해주세요.";
                textToSpeech.speak(sentence, TextToSpeech.QUEUE_FLUSH, null,null);
                Handler delayHandler = new Handler();
                delayHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // TODO
                        createRecognizer();
                    }
                }, 4500);


            }

        }
    }

    public void goToCamera() {
        stopListening();
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }






    //text to speech stop 시키는 메소드
    public void createRecognizer(){

        Intent intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,"com.example.ds.myapplication");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR");

        mRecognizer=SpeechRecognizer.createSpeechRecognizer(this);
        mRecognizer.setRecognitionListener(listener);

        mRecognizer.startListening(intent);

    }

    //음성인식 객체 중지
    private void stopListening()
    {
        if(mRecognizer != null)
        {
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
