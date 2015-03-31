package examples.amorg.aut.bme.hu.soundchart;

import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.achartengine.GraphicalView;

public class MainActivity extends ActionBarActivity {

    private class RecordThread extends Thread {
        public void run() {
            startRecording();
            while (readEnabled) {

                buffer = new short[AudioRecord.getMinBufferSize(44100,
                        channelConfiguration,
                        audioEncoding)];
                recorder.read(buffer, 0, buffer.length);

                final LineGraph line = new LineGraph();
                for (int i = 0; i < 2048; i++) {
                    line.addNewPoints(i, buffer[i]);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(MainActivity.this, "test...", Toast.LENGTH_SHORT).show();
                        view = line.getView(MainActivity.this);
                        chartContainer.removeAllViews();
                        chartContainer.addView(view);
                    }
                });

                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean readEnabled = true;

    private static GraphicalView view;

    AudioRecord recorder = null;
    private static int audioEncoding = 2;
    private static int channelConfiguration = 16;
    static short[] buffer;
    private LinearLayout chartContainer;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        chartContainer = (LinearLayout) findViewById(R.id.chart);


    }

    private void startRecording() {
        if (recorder != null) {
            recorder.release();
        }

        recorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, 44100,
                channelConfiguration, audioEncoding,
                AudioRecord.getMinBufferSize(44100, channelConfiguration, audioEncoding));

        recorder.startRecording();
    }


    @Override
    protected void onStart() {
        super.onStart();
        startRecordingThread();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopRecording();
    }

    private void startRecordingThread() {
        readEnabled = true;
        new RecordThread().start();
    }

    private void stopRecording() {
        readEnabled = false;
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
    }


    public void buttonTapped(View viewButton) {
        switch (viewButton.getId()) {
            case R.id.startbtn:
                startRecordingThread();
                break;
            case R.id.stopbtn:
                stopRecording();
                break;
        }
    }

}


