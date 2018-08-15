package com.av.whereareyou;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RecordActivity extends Activity {

    private static final int RECORDER_BPP = 16;
    private static int RECORDER_SAMPLERATE = 8000;
    private static int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private static final String TAG = "RecordActivity";

    @BindView(R.id.btn_mic)
    ImageButton btnMic;
    @BindView(R.id.btn_stop)
    ImageButton btnStop;
    @BindView(R.id.btn_play)
    ImageButton btnPlay;
    @BindView(R.id.btn_test)
    Button btnTest;

    private MediaRecorder mediaRecorder;

    String filePath;
    MediaPlayer mediaPlayer;

    private Camera camera;
    private boolean isFlashOn;
    private boolean hasFlash;
    private android.hardware.Camera.Parameters params;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        ButterKnife.bind(this);

        btnPlay.setVisibility(View.GONE);
        btnTest.setVisibility(View.GONE);

        mediaRecorder = new MediaRecorder();

        filePath = getFilesDir().getAbsolutePath() + "record.wav";

        File file = new File(filePath);
        if (file.exists()) {
            btnPlay.setVisibility(View.VISIBLE);
            btnTest.setVisibility(View.VISIBLE);
        } else {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        hasFlash = getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (!hasFlash) {
            // device doesn't support flash
            // Show alert message and close the application
            AlertDialog alert = new AlertDialog.Builder(RecordActivity.this)
                    .create();
            alert.setTitle("Error");
            alert.setMessage("Sorry, your device doesn't support flash light!");
            alert.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // closing the application
                    finish();
                }
            });
            alert.show();
        }

        getCamera();

    }



    @OnClick(R.id.btn_play)
    public void onPlay(View view) {

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);

        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
        } catch (IOException e) {
            Log.d(TAG, e.toString());
        }
        mediaPlayer.start();
        btnPlay.setVisibility(View.GONE);

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mediaPlayer.release();
                btnPlay.setVisibility(View.VISIBLE);
            }
        });
        Toast.makeText(this, "playing", Toast.LENGTH_SHORT).show();
    }


    @OnClick(R.id.btn_mic)
    public void onMic(View view) {

//        resetRecorder();
//
//        try {
//            mediaRecorder.prepare();
//            mediaRecorder.start();
//        } catch (IllegalStateException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        btnMic.setVisibility(View.GONE);
        btnStop.setVisibility(View.VISIBLE);

        record();
    }

//    @OnClick(R.id.btn_stop)
//    public void onRecordStop(View view) {
//        mediaRecorder.stop();
//        mediaRecorder.release();
//        mediaRecorder = null;
//
//        btnStop.setVisibility(View.GONE);
//        btnMic.setVisibility(View.VISIBLE);
//        btnPlay.setVisibility(View.VISIBLE);
//    }

    @OnClick(R.id.btn_test)
    public void gotoTestMode(View view) {
        Intent intent = new Intent(RecordActivity.this, TestModeActivity.class);
        startActivity(intent);
    }

    // this process must be done prior to the start of recording
    private void resetRecorder() {
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setAudioEncodingBitRate(16);
        mediaRecorder.setAudioSamplingRate(44100);
        mediaRecorder.setOutputFile(filePath);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    @Override
    public void onBackPressed() {

    }

    private void record() {
        int bufferSizeInBytes = AudioRecord.getMinBufferSize( RECORDER_SAMPLERATE,
                RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING
        );

        // Initialize Audio Recorder.
        AudioRecord audioRecorder = new AudioRecord( MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE,
                RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING,
                bufferSizeInBytes
        );
        // Start Recording.
        audioRecorder.startRecording();

        int numberOfReadBytes   = 0;
        byte audioBuffer[]      = new  byte[bufferSizeInBytes];
        boolean recording       = false;
        float tempFloatBuffer[] = new float[3];
        int tempIndex           = 0;
        int totalReadBytes      = 0;
        byte totalByteBuffer[]  = new byte[60 * 44100 * 2];


        // While data come from microphone.
        while( true )
        {
            float totalAbsValue = 0.0f;
            short sample        = 0;

            numberOfReadBytes = audioRecorder.read( audioBuffer, 0, bufferSizeInBytes );
            Log.d( TAG, "======" + String.valueOf(numberOfReadBytes));

            // Analyze Sound.
            for( int i=0; i<bufferSizeInBytes; i+=2 )
            {
                sample = (short)( (audioBuffer[i]) | audioBuffer[i + 1] << 8 );
                totalAbsValue += Math.abs( sample ) / (numberOfReadBytes/2);
            }

            // Analyze temp buffer.
            tempFloatBuffer[tempIndex%3] = totalAbsValue;
            float temp                   = 0.0f;
            for( int i=0; i<3; ++i )
                temp += tempFloatBuffer[i];

            if( (temp >=0 && temp <= 350) && recording == false )
            {
                Log.i(TAG, "1");
                tempIndex++;
                continue;
            }

            if( temp > 350 && recording == false )
            {
                Log.i(TAG, "2");
                recording = true;
            }

            if( (temp >= 0 && temp <= 350) && recording == true )
            {
                Log.i(TAG, "Save audio to file.");

                long totalAudioLen  = 0;
                long totalDataLen   = totalAudioLen + 36;
                long longSampleRate = RECORDER_SAMPLERATE;
                int channels        = 1;
                long byteRate       = RECORDER_BPP * RECORDER_SAMPLERATE * channels/8;
                totalAudioLen       = totalReadBytes;
                totalDataLen        = totalAudioLen + 36;
                byte finalBuffer[]  = new byte[totalReadBytes + 44];

                finalBuffer[0] = 'R';  // RIFF/WAVE header
                finalBuffer[1] = 'I';
                finalBuffer[2] = 'F';
                finalBuffer[3] = 'F';
                finalBuffer[4] = (byte) (totalDataLen & 0xff);
                finalBuffer[5] = (byte) ((totalDataLen >> 8) & 0xff);
                finalBuffer[6] = (byte) ((totalDataLen >> 16) & 0xff);
                finalBuffer[7] = (byte) ((totalDataLen >> 24) & 0xff);
                finalBuffer[8] = 'W';
                finalBuffer[9] = 'A';
                finalBuffer[10] = 'V';
                finalBuffer[11] = 'E';
                finalBuffer[12] = 'f';  // 'fmt ' chunk
                finalBuffer[13] = 'm';
                finalBuffer[14] = 't';
                finalBuffer[15] = ' ';
                finalBuffer[16] = 16;  // 4 bytes: size of 'fmt ' chunk
                finalBuffer[17] = 0;
                finalBuffer[18] = 0;
                finalBuffer[19] = 0;
                finalBuffer[20] = 1;  // format = 1
                finalBuffer[21] = 0;
                finalBuffer[22] = (byte) channels;
                finalBuffer[23] = 0;
                finalBuffer[24] = (byte) (longSampleRate & 0xff);
                finalBuffer[25] = (byte) ((longSampleRate >> 8) & 0xff);
                finalBuffer[26] = (byte) ((longSampleRate >> 16) & 0xff);
                finalBuffer[27] = (byte) ((longSampleRate >> 24) & 0xff);
                finalBuffer[28] = (byte) (byteRate & 0xff);
                finalBuffer[29] = (byte) ((byteRate >> 8) & 0xff);
                finalBuffer[30] = (byte) ((byteRate >> 16) & 0xff);
                finalBuffer[31] = (byte) ((byteRate >> 24) & 0xff);
                finalBuffer[32] = (byte) (2 * 16 / 8);  // block align
                finalBuffer[33] = 0;
                finalBuffer[34] = RECORDER_BPP;  // bits per sample
                finalBuffer[35] = 0;
                finalBuffer[36] = 'd';
                finalBuffer[37] = 'a';
                finalBuffer[38] = 't';
                finalBuffer[39] = 'a';
                finalBuffer[40] = (byte) (totalAudioLen & 0xff);
                finalBuffer[41] = (byte) ((totalAudioLen >> 8) & 0xff);
                finalBuffer[42] = (byte) ((totalAudioLen >> 16) & 0xff);
                finalBuffer[43] = (byte) ((totalAudioLen >> 24) & 0xff);

                for( int i=0; i<totalReadBytes; ++i )
                    finalBuffer[44+i] = totalByteBuffer[i];

                FileOutputStream out;
                try {
                    out = new FileOutputStream(filePath);
                    try {
                        out.write(finalBuffer);
                        out.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                } catch (FileNotFoundException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

                //*/
                tempIndex++;

                Log.i(TAG, "End");

                audioRecorder.stop();

                stopRecord();
                break;
            }

            // -> Recording sound here.
            Log.i( TAG, "Recording Sound." );
            for( int i=0; i<numberOfReadBytes; i++ )
                totalByteBuffer[totalReadBytes + i] = audioBuffer[i];
            totalReadBytes += numberOfReadBytes;
            //*/

            tempIndex++;

        }
    }

    private void stopRecord() {
        btnStop.setVisibility(View.GONE);
        btnMic.setVisibility(View.VISIBLE);
        btnPlay.setVisibility(View.VISIBLE);

        turnOnFlash();
    }

    private void getCamera() {
        if (camera == null) {
            try {
                camera = Camera.open();
                params = camera.getParameters();
            } catch (RuntimeException e) {
                Log.e(TAG, "Camera Error. Failed to Open. Error: " + e.getMessage());
            }
        }
    }

    private void turnOnFlash() {
        Log.d(TAG, "======" + String.valueOf(isFlashOn));
        if (!isFlashOn) {
            if (camera == null || params == null) {
                return;
            }

            params = camera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(params);
            camera.startPreview();
            isFlashOn = true;
        }

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                turnOffFlash();
            }
        }, 1000);
    }

    private void turnOffFlash() {
        if (isFlashOn) {
            if (camera == null || params == null) {
                return;
            }

            params = camera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(params);
            camera.stopPreview();
            isFlashOn = false;
        }
    }
}
