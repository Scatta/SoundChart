package examples.amorg.aut.bme.hu.soundchart;

import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.achartengine.GraphicalView;


public class MainActivity extends ActionBarActivity {

    private boolean readEnabled = true;

    private static GraphicalView view;

    AudioRecord recorder = null;
    private static int audioEncoding = 2;
    private static int channelConfiguration = 16;
    static byte[] buffer;
    static byte[] bufferHold;
    private LinearLayout chartContainer;

    private FFT fourier;

    private double[] spectrum;
    private double[] spectrumHold;

    private double max;
    private double maxHold;
    private double speed;

    TextView tvText;

    enum mEnum {Start,Stop};
    private mEnum btnStartStop;


    private class RecordThread extends Thread {
        public void run() {
            startRecording();
            fourier = new FFT();
            bufferHold = null;
            spectrumHold = null;

            while (readEnabled) {

                buffer = new byte[AudioRecord.getMinBufferSize(44100,
                        channelConfiguration,
                        audioEncoding)];
                recorder.read(buffer, 0, buffer.length);

                spectrum = fourier.doFFT(buffer);

                final LineGraph line = new LineGraph();
                for (int i = 0; i < spectrum.length / 2; i++) {
                    line.addNewPoints((i * 22050 / fourier.getFftSize()) , spectrum[i]);  //Math.log10(spectrum[i]/1000)
                }


                if(spectrumHold != null) {
                    for (int i = 0; i < spectrumHold.length / 2; i++) {
                        line.addNewPoints_line2((i * 22050 / fourier.getFftSize()), spectrumHold[i]);
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(MainActivity.this, "test...", Toast.LENGTH_SHORT).show();
                        view = line.getView(MainActivity.this);
                        chartContainer.removeAllViews();
                        chartContainer.addView(view);
                        max=fourier.findMax(spectrum)*22050/fourier.getFftSize();
                        tvText.setText("Domináns frekvencia: " + ""+max+" Hz\n" +
                                       "Referencia frekvencia: 0.0 Hz\n" +
                                       "Sebesség: 0 m/s");
                        if(spectrumHold != null) {
                            maxHold = fourier.findMax(spectrumHold)*22050/fourier.getFftSize();
                            speed = 299792458*((max/maxHold)-1);
                            tvText.setText("Domináns frekvencia: " + ""+max+" Hz\n" +
                                           "Referencia frekvencia: " + ""+maxHold+" Hz\n" +
                                           "Sebesség: " + speed + " m/s");
                        }
                    }
                });

            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE); // nem működik!
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chartContainer = (LinearLayout) findViewById(R.id.chart);

        tvText = (TextView) findViewById(R.id.tv);

        btnStartStop = mEnum.Stop;

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

    public void buttonStartStop(View viewButton) {
        Button mBtn = (Button) findViewById(R.id.startstopbtn);
        switch (btnStartStop) {
            case Start:
                startRecordingThread();
                mBtn.setText("Leállítás");
                btnStartStop = mEnum.Stop;
                break;
            case Stop:
                stopRecording();
                mBtn.setText("Felvétel");
                btnStartStop = mEnum.Start;
                break;
        }
    }

    public void buttonFreeze(View viewFreeze) {
        bufferHold = new byte[buffer.length];
        bufferHold = buffer;
        spectrumHold = fourier.doFFT(bufferHold);

    }

}


