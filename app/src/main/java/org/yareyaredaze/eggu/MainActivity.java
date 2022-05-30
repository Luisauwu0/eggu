package org.yareyaredaze.eggu;

import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.yareyaredaze.eggu.databinding.ActivityMainBinding;

import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    public static final int REQUEST_AUDIO_PERMISSION_CODE = 200;
    private boolean status = true;

    private final String TAG = "EGGU";

    private static final long TRACK_TIME = 10000;
    private static final long DEFAULT_WAIT_TIME = 1000;

    public static final int AMPLITUDE_DIFF_LOW = 10000;
    public static final int AMPLITUDE_DIFF_HIGH = 26000;
    public static final int AMPLITUDE_DIFF_DEFAULT =  18000;

    private boolean continueRecording = true;
    private Thread recordThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mediaPlayer = MediaPlayer.create(this, R.raw.hensound);

        final int MAX_VOLUME = 100;
        binding.egguVolBar.setMax(100);
        binding.egguVolBar.setProgress(10);
        binding.egguVolBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "Volume: "+ seekBar.getProgress());
                float volume =(float) (1 - (Math.log(MAX_VOLUME - seekBar.getProgress()) / Math.log(MAX_VOLUME)));
                Log.d(TAG, "vol: " + volume);
                mediaPlayer.setVolume(volume, volume);
                binding.egguTxt.setText("Current Hen Sound Volume: " + seekBar.getProgress() + "%");
            }
        });

        binding.egguAmpBar.setMax(AMPLITUDE_DIFF_HIGH);
        binding.egguAmpBar.setProgress(2000);
        binding.egguAmpBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0 ;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "Amplitude " + seekBar.getProgress());
                binding.egguTxt.setText("Current Amplitude: " + seekBar.getProgress());
            }
        });

        binding.egguBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (status) {
                    binding.egguBtn.setBackgroundColor(getResources().getColor(R.color.teal_700, getResources().newTheme()));
                    binding.egguBtn.setText("Egging");
                    try {
                        startRecording();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "Started");
                } else {
                    try {
                        pauseRecording();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    binding.egguBtn.setBackgroundColor(getResources().getColor(R.color.green, getResources().newTheme()));
                    binding.egguBtn.setText("Egg");
                }
                status = !status;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean CheckPermissions() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    private void RequestPermissions() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE}, REQUEST_AUDIO_PERMISSION_CODE);
    }

    private void startRecording() throws IOException {
        if (CheckPermissions()) {
            binding.egguAmpBar.setEnabled(true);
            binding.egguVolBar.setEnabled(true);
            continueRecording = true;
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(getRecordingFilePath());
            try {
                mediaRecorder.prepare();
            } catch (IOException e) {
                Log.e(TAG, "prepare() failed");
            }
            mediaRecorder.start();
            binding.egguBtn.setText("Egging");
            recordThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    reactOnRecord();
                }
            });
            recordThread.start();
        } else {
            RequestPermissions();
        }
    }

    private void reactOnRecord() {
        Random random = new Random();
        int startAmplitude = mediaRecorder.getMaxAmplitude();
        Log.d(TAG, "Starting Amplitude " + startAmplitude);
        while (continueRecording) {
            waitSome(DEFAULT_WAIT_TIME);
            int finishAmplitude = mediaRecorder.getMaxAmplitude();
            int ampDifference = finishAmplitude - startAmplitude;
            Log.d(TAG, "Amplitude Difference: " + ampDifference);
            if (ampDifference >= binding.egguAmpBar.getProgress()) {
                Log.d(TAG, "Starting Hen Sound");
                mediaPlayer.seekTo(random.nextInt(mediaPlayer.getDuration() - (int) TRACK_TIME));
                mediaPlayer.start();
                waitSome(TRACK_TIME);
                mediaPlayer.pause();
                Log.d(TAG, "Stopped Hen Sound");
            }
        }
    }

    private void pauseRecording() throws InterruptedException {
        continueRecording = false;
        recordThread.join();
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
        binding.egguBtn.setBackgroundColor(getResources().getColor(R.color.teal_700, getResources().newTheme()));
        binding.egguBtn.setText("Eggu");
        binding.egguAmpBar.setEnabled(false);
        binding.egguVolBar.setEnabled(false);
    }

    private String getRecordingFilePath() {
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File file = new File(musicDirectory, "recording_file" + ".mp3");
        return file.getPath();
    }

    private void waitSome(long waitTime) {
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e){
            Log.d(TAG, "Waiting was interrupted");
        }
    }
}