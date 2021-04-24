package com.example.ds.myapplication;

//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.app.Activity;
import android.view.Window;

import java.io.ByteArrayOutputStream;
import java.util.Locale;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

//커스텀 카메라 액티비티
public class CameraActivity extends Activity {
    public static String IMAGE_FILE = "capture.jpg";
    private long mLastClickTime = 0;
    CameraSurfaceView cameraView;
    TextToSpeech textToSpeech;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE); //타이틀 바 없앰
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // 화면 고정
        setContentView(R.layout.activity_camera);
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {

                    int result = textToSpeech.setLanguage(Locale.KOREA);

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(CameraActivity.this, "이 언어는 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
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
                //Do something after 100ms

                String sentence = "카메라가 실행되었습니다. 아무곳이나 터치하시면 사진이 찍힙니다.";
                textToSpeech.speak(sentence, TextToSpeech.QUEUE_FLUSH, null,null);

                //mRecognizer.startListening(intent);

            }
        }, 300);
        cameraView = new CameraSurfaceView(getApplicationContext());
        FrameLayout previewFrame = findViewById(R.id.previewFrame);
        LinearLayout previewFrame2 = findViewById(R.id.previewFrame2);
        previewFrame.addView(cameraView);


        //권한 물어보기
        checkDangerousPermissions();
    }

    private void checkDangerousPermissions() {
        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        };

        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        for (int i = 0; i < permissions.length; i++) {
            permissionCheck = ContextCompat.checkSelfPermission(this, permissions[i]);
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                break;
            }
        }

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {

        } else {


            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {

            } else {
                ActivityCompat.requestPermissions(this, permissions, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {

                } else {

                }
            }
        }
    }

    public void captured(View view) {

        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        cameraView.capture(new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {

                try {


                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    //사진 90도로 돌아가는 것 해결
                    bitmap = RotateBitmap(bitmap, 90);
                    String outUriStr = MediaStore.Images.Media.insertImage(CameraActivity.this.getContentResolver(),
                            bitmap, "Captured Image", "Captured Image using Camera.");


                    if (outUriStr == null) {


                        return;
                    } else {


                        //여기가 사진 경로값(outUri) 만들어서 결과 액티비티로 인텐트 보내주는 메소드
                        Uri outUri = Uri.parse(outUriStr);
                        CameraActivity.this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, outUri));
                        Log.e("camera", "camera 저장되었음");

                        //여기가 비트맵을 Result Activity로 보내는 intent
                        Intent intent = new Intent(CameraActivity.this, ResultActivity.class);


                        /*인텐트를 byteArray나 bitmap으로 넘기면 가상 에뮬레이터에선 잘 실행이 되나, 실제 단말에서는 이미지 크기가 대개 1M를
                        넘기므로 어플이 종료됩니다.(intent로 전달하는 용량 한계가 1M이라고 함
                        그래서 bytearray말고 uri 값으로 넘겨주었습니다.*/
                        //intent.putExtra("image", byteArray);
                        intent.putExtra("uri", outUri);
                        textToSpeech.stop();
                        textToSpeech.shutdown();
                        CameraActivity.this.finish();
                        CameraActivity.this.startActivity(intent);


                    }


                    camera.startPreview();
                } catch (Exception e) {

                }


            }
        });

    }
    //안드로이드 기본 카메라로 찍은 이미지 회전 오류 복구 시키는 메소드
    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }


    //카메라 미리보기를 위한 서피스뷰 정의
    private class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
        private SurfaceHolder mHolder;
        private Camera camera = null;

        public CameraSurfaceView(Context context) {
            super(context);

            mHolder = getHolder();
            mHolder.addCallback(this);

        }

        private void setFocus(String mParameter) {
            Camera.Parameters mParameters = camera.getParameters();
            mParameters.setFocusMode(mParameter);
            camera.setParameters(mParameters);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            camera = Camera.open();
            camera.setDisplayOrientation(90);



            try {
                camera.setPreviewDisplay(mHolder);
            } catch (Exception e) {

            }
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            setFocus(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);


            // Start camera preview
            camera.startPreview();
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }

        public boolean capture(Camera.PictureCallback handler) {
            if (camera != null) {
                camera.takePicture(null, null, handler);
                return true;
            } else {
                return false;
            }
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        textToSpeech.stop();
        textToSpeech.shutdown();
    }
}