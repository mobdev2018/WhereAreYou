package com.av.whereareyou;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.vikramezhil.droidspeech.DroidSpeech;
import com.vikramezhil.droidspeech.OnDSListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TestColorActivity extends Activity {

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.slidermenu)
    View mDrawerMenu;
    @BindView(R.id.list_slidermenu)
    ListView mDrawerList;

    @BindView(R.id.contentView)
    View contentView;
    @BindView(R.id.btn_ok)
    Button btnOK;
    @BindView(R.id.txtVoice)
    TextView txtVoice;

    @BindView(R.id.btn_back)
    ImageButton btnBack;
    @BindView(R.id.btn_settings)
    ImageButton btnSettings;

    private static String TAG = "TestColorActivity";
    private FirebaseAuth firebaseAuth;

    String testFilePath;

    private Camera camera;
    private boolean isFlashOn = false;
    private boolean hasFlash;
    private android.hardware.Camera.Parameters params;

    DroidSpeech droidSpeech;
    Vibrator vib;
    Ringtone ringtone;

    Timer vibrationTimer;
    TimerTask vibrationTask;

    private String fNmae = "audio.mp3";
    private String fPAth = "android.resource://com.av.whereareyou/raw/audio";

    ArrayList<HashMap<String, String>> listMenu = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_color);
        ButterKnife.bind(this);

        firebaseAuth = FirebaseAuth.getInstance();

        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        String color = sharedPreferences.getString("color", "blue");
        setBackground(color);

        btnOK.setVisibility(View.GONE);

        testFilePath = getFilesDir().getAbsolutePath() + "record.wav";

        File file = new File(testFilePath);
        if (!file.exists()) {
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
            AlertDialog alert = new AlertDialog.Builder(TestColorActivity.this)
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


        AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        switch (am.getRingerMode()) {
            case AudioManager.RINGER_MODE_SILENT:
                Log.i("MyApp","Silent mode");
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                Log.i("MyApp","Vibrate mode");
                break;
            case AudioManager.RINGER_MODE_NORMAL:
                Log.i("MyApp","Normal mode");
                break;
        }

        try {
            am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        ringtone = RingtoneManager.getRingtone(this, RingtoneManager.getValidRingtoneUri(this));

        vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startRecord();
            }
        }, 1000);

        disableBeepSound();

        setMenuList();
    }

    private void disableBeepSound() {
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        if (am != null) {
            am.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, AudioManager.FLAG_PLAY_SOUND);
            am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_PLAY_SOUND);
            am.setStreamVolume(AudioManager.STREAM_ALARM, 0, AudioManager.FLAG_PLAY_SOUND);
            am.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, AudioManager.FLAG_PLAY_SOUND);
        }
    }

    private void enableBeepSound() {
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        if (am != null) {
            am.setStreamVolume(AudioManager.STREAM_SYSTEM, 1, AudioManager.FLAG_PLAY_SOUND);
            am.setStreamVolume(AudioManager.STREAM_MUSIC, 1, AudioManager.FLAG_PLAY_SOUND);
            am.setStreamVolume(AudioManager.STREAM_ALARM, 1, AudioManager.FLAG_PLAY_SOUND);
            am.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 1, AudioManager.FLAG_PLAY_SOUND);
        }
    }

    private void startRecord() {
        droidSpeech = new DroidSpeech(this, null);
        droidSpeech.setOnDroidSpeechListener(new OnDSListener() {
            @Override
            public void onDroidSpeechSupportedLanguages(String currentSpeechLanguage, List<String> supportedSpeechLanguages) {
                Log.d(TAG, currentSpeechLanguage);
            }

            @Override
            public void onDroidSpeechRmsChanged(float rmsChangedValue) {
//                Log.d(TAG, "rms");
            }

            @Override
            public void onDroidSpeechLiveResult(String liveSpeechResult) {
                Log.d(TAG, liveSpeechResult);
                txtVoice.setText(liveSpeechResult);
            }

            @Override
            public void onDroidSpeechFinalResult(String finalSpeechResult) {
                Log.d(TAG, finalSpeechResult);
                txtVoice.setText(finalSpeechResult);
                if (finalSpeechResult.equals("where are you")) {
                    droidSpeech.closeDroidSpeechOperations();
                    enableBeepSound();
                    detected();
                }
            }

            @Override
            public void onDroidSpeechClosedByUser() {
                Log.d(TAG, "onDroidSpeechClosedByUser");
            }

            @Override
            public void onDroidSpeechError(String errorMsg) {
                Log.d(TAG, "error: " + errorMsg);
            }
        });

        droidSpeech.startDroidSpeechRecognition();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (ringtone.isPlaying()) {
            ringtone.stop();
        }

        if (vibrationTimer != null) {
            vibrationTimer.cancel();
        }

        vib.cancel();
        turnOffFlash();
    }

    @Override
    public void onDestroy() {
        droidSpeech.closeDroidSpeechOperations();

        if (ringtone.isPlaying()) {
            ringtone.stop();
        }

        if (vibrationTimer != null) {
            vibrationTimer.cancel();
        }

        vib.cancel();
        turnOffFlash();

        super.onDestroy();
    }

    @OnClick(R.id.btn_back)
    public void onBack(View view) {
        droidSpeech.closeDroidSpeechOperations();

        if (ringtone.isPlaying()) {
            ringtone.stop();
        }

        if (vibrationTask != null) {
            vibrationTask.cancel();
        }

        turnOffFlash();
        vib.cancel();

        finish();
    }

    @OnClick(R.id.btn_settings)
    public void onSettins(View view) {
        if (mDrawerLayout.isDrawerOpen(mDrawerMenu)) {
            mDrawerLayout.closeDrawer(mDrawerMenu);
        } else {
            mDrawerLayout.openDrawer(mDrawerMenu);
        }
    }

    @OnClick(R.id.btn_ok)
    public void onOk(View view) {
        droidSpeech.closeDroidSpeechOperations();

        if (ringtone.isPlaying()) {
            ringtone.stop();
        }

        if (vibrationTask != null) {
            vibrationTask.cancel();
        }

        if (vibrationTimer != null) {
            vibrationTimer.cancel();
        }

        vib.cancel();
        turnOffFlash();

        Intent intent = new Intent(TestColorActivity.this, ConfirmRecordActivity.class);
        startActivity(intent);
    }

    private void detected() {

        btnOK.setVisibility(View.VISIBLE);

        vibrationTimer = new Timer();
        vibrationTask = new TimerTask() {
            @Override
            public void run() {
                turnOnFlash();
                vib.vibrate(1000);
            }
        };

        vibrationTimer.scheduleAtFixedRate(vibrationTask, 0, 1200);

        ringtone.play();

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

        turnOffFlash();
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


    private void setBackground(String colorString) {
        if (colorString.equals("blue")) {
            contentView.setBackgroundResource(R.color.blue);
        } else if (colorString.equals("orange")) {
            contentView.setBackgroundResource(R.color.orange);
        } else if (colorString.equals("pink")) {
            contentView.setBackgroundResource(R.color.pink);
        } else if (colorString.equals("red")) {
            contentView.setBackgroundResource(R.color.red);
        } else if (colorString.equals("green")) {
            contentView.setBackgroundResource(R.color.green);
        } else if (colorString.equals("white")) {
            contentView.setBackgroundResource(R.color.white);
            btnBack.setImageDrawable(getDrawable(R.drawable.back_black));
            btnSettings.setImageDrawable(getDrawable(R.drawable.settings_black));
            btnOK.setBackgroundResource(R.drawable.black_border_button);
            btnOK.setTextColor(getResources().getColor(R.color.black, null));
            txtVoice.setTextColor(getResources().getColor(R.color.black, null));
        }
    }


    private void setMenuList() {
        listMenu.clear();

        HashMap<String, String> hashMap1 = new HashMap<>();
        hashMap1.put("category", "Flashes");

        HashMap<String, String> hashMap2 = new HashMap<>();
        hashMap2.put("category", "Color");

        HashMap<String, String> hashMap3 = new HashMap<>();
        hashMap3.put("category", "Ringtones");

        HashMap<String, String> hashMap4 = new HashMap<>();
        hashMap4.put("category", "Power Saving");

        HashMap<String, String> hashMap5 = new HashMap<>();
        hashMap5.put("category", "Sign Out");


        listMenu.add(hashMap1);
        listMenu.add(hashMap2);
        listMenu.add(hashMap3);
        listMenu.add(hashMap4);
        listMenu.add(hashMap5);

        mDrawerList.setItemChecked(0, true);
        mDrawerList.setSelection(0);
        mDrawerLayout.closeDrawer(mDrawerMenu);
        mDrawerList.setAdapter(new MenuListAdapter(getApplicationContext(), listMenu, 0));
        enableSlideMenu();
    }

    public void displayView(int tabIndex) {
        mDrawerLayout.closeDrawer(mDrawerMenu);
        switch (tabIndex) {
            case 0:
                Intent intentFlash = new Intent(TestColorActivity.this, FlashPatternsActivity.class);
                startActivity(intentFlash);
                break;
            case 1:
                Intent intentColor = new Intent(TestColorActivity.this, ColorsActivity.class);
                startActivity(intentColor);
                break;
            case 2:
                Intent intentRingtones = new Intent(TestColorActivity.this, RingtonesActivity.class);
                startActivity(intentRingtones);
                break;
            case 3:
                Intent intentWarning = new Intent(TestColorActivity.this, WarningActivity.class);
                startActivity(intentWarning);
                break;
            case 4: // log out
                firebaseAuth.signOut();
                SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("email");
                editor.remove("password");
                editor.apply();

                Intent intentLogin = new Intent(TestColorActivity.this, LoginActivity.class);
                startActivity(intentLogin);
                break;
        }

        finish();

    }

    public class MenuListAdapter extends BaseAdapter {

        private Context mContext;
        private LayoutInflater inflater = null;
        ArrayList<HashMap<String, String>> localList;
        int selectedPosition;

        public MenuListAdapter(Context context, ArrayList<HashMap<String, String>> localList, int selectedPosition) {
            this.mContext = context;
            this.localList = localList;
            this.selectedPosition = selectedPosition;
            inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return localList.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(final int position, View view, ViewGroup viewGroup) {
            MenuViewHolder holder;

            view = inflater.inflate(R.layout.layout_menulist_row, null);
            holder = new MenuViewHolder(view);
            view.setTag(holder);

            if (position == selectedPosition) {
                holder.rlMenuItem.setBackgroundColor(getResources().getColor(R.color.gray_selected));
            } else {
                holder.rlMenuItem.setBackgroundColor(getResources().getColor(R.color.transparent));
            }

            holder.txtMenuTitle.setText(localList.get(position).get("category"));

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedPosition = position;

                    switch (position) {
                        default:
                            displayView(position);
                            break;
                    }
                }
            });

            return view;
        }

    }

    static class MenuViewHolder {

        @BindView(R.id.rlMenuItem)
        LinearLayout rlMenuItem;
        @BindView(R.id.menu_title)
        TextView txtMenuTitle;

        public MenuViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public void enableSlideMenu() {
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    public void disableSlideMenu() {
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

}
