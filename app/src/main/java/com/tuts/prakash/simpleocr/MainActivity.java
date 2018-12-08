package com.tuts.prakash.simpleocr;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {

    SurfaceView mCameraView;
    TextView mTextView;
    CameraSource mCameraSource;

    private static final String TAG = "MainActivity";
    private static final int requestPermissionIDcamera = 101;
    private static final int requestPermissionIDfile = 1000;
    private String texttosave = "No Text Yet";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestPermissionIDfile);
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        requestPermissionIDfile);
            }
        } catch (Exception e) {
            Context context = getApplicationContext();
            Toast toast = Toast.makeText(context, "file error", Toast.LENGTH_LONG);
            toast.show();
            e.printStackTrace();
        }
        mCameraView = findViewById(R.id.surfaceView);
        mTextView = findViewById(R.id.text_view);
        final Button saveButton = findViewById(R.id.SaveText);
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Context context = getApplicationContext();
                /*try {

                    if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                requestPermissionIDfile);
                        Toast savetoast = Toast.makeText(context, "returning", Toast.LENGTH_LONG);
                        savetoast.show();
                        return;
                    }
                    //mCameraSource.start(mCameraView.getHolder());
                } catch (Exception e) {
                    Toast permissionsToast = Toast.makeText(context, e.toString(), Toast.LENGTH_LONG);
                    permissionsToast.show();
                    e.printStackTrace();
                }*/

                CharSequence text = "Saving";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                Log.d("SaveButton was clicked", "savebutton");
                // Thanks to https://stackoverflow.com/questions/28755934/
                // android-studio-writing-text-to-a-file-using-a-save-button-in-the-menu for snippet
                String filename = "OCR_Text.txt";
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), filename);
                Toast filer = Toast.makeText(context, "test" + getFileStreamPath(filename).toString(), duration);
                //filer.show();

                try {
                    FileOutputStream output = new FileOutputStream(file);
                    //OutputStreamWriter output = new OutputStreamWriter(openFileOutput("TextReaderFile", MODE_APPEND));
                    //EditText ET = (EditText)findViewById(R.id.editText);
                    //String text = ET.getText().toString();
                    output.write(texttosave.getBytes());
                    output.close();
                    Toast fileToast = Toast.makeText(context, "The contents are saved in the file.", duration);
                    fileToast.show();
                } catch (FileNotFoundException e) {
                    //Toast.makeText(this, "Exception: " + t.toString(), Toast.LENGTH_LONG).show();
                    Toast toaster = Toast.makeText(context, e.toString(), duration);
                    toaster.show();
                    e.printStackTrace();
                } catch (IOException e) {
                    Toast toasting = Toast.makeText(context, e.toString(), duration);
                    toasting.show();
                    e.printStackTrace();
                }
            }
        });
        final Button homeScreen = findViewById(R.id.HomeScreen);
        homeScreen.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Context context = getApplicationContext();
                CharSequence text = "Moving to Home";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                Log.d("HomeScreen was clicked", "homescreen");
                openHomeActivity();
            }
        });
        startCameraSource();
    }
    public void openHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != requestPermissionIDcamera || requestCode != requestPermissionIDfile) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mCameraSource.start(mCameraView.getHolder());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void startCameraSource() {

        //Create the TextRecognizer
        final TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        if (!textRecognizer.isOperational()) {
            Log.w(TAG, "Detector dependencies not loaded yet");
        } else {

            //Initialize camerasource to use high resolution and set Autofocus on.
            mCameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280, 1024)
                    .setAutoFocusEnabled(true)
                    .setRequestedFps(2.0f)
                    .build();

            /**
             * Add call back to SurfaceView and check if camera permission is granted.
             * If permission is granted we can start our cameraSource and pass it to surfaceView
            */
            mCameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    try {

                        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    requestPermissionIDcamera);
                            return;
                        }
                        mCameraSource.start(mCameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    mCameraSource.stop();
                }
            });

            //Set the TextRecognizer's Processor.
            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {
                }

                /**
                 * Detect all the text from camera using TextBlock and the values into a stringBuilder
                 * which will then be set to the textView.
                 * */
                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {
                    final SparseArray<TextBlock> items = detections.getDetectedItems();
                    if (items.size() != 0 ){

                        mTextView.post(new Runnable() {
                            @Override
                            public void run() {
                                StringBuilder stringBuilder = new StringBuilder();
                                for(int i=0;i<items.size();i++){
                                    TextBlock item = items.valueAt(i);
                                    stringBuilder.append(item.getValue());
                                    stringBuilder.append("\n");
                                }
                                mTextView.setText(stringBuilder.toString());
                                texttosave = stringBuilder.toString();
                            }
                        });
                    }
                }
            });
        }
    }
}
