package examples.amorg.aut.bme.hu.soundchart;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.achartengine.GraphicalView;


public class MainActivity extends ActionBarActivity {

    private boolean readEnabled = true;

    private static GraphicalView view;

    AudioRecord recorder = null;
    private static int audioEncoding = 2;
    private static int channelConfiguration = 16;
    private static byte[] buffer;
    private static byte[] bufferHold;
    private static byte[] bufferRecord;

    private LinearLayout chartContainer;

    private FFT fourier;

    private double[] spectrum;
    private double[] spectrumHold;
    private int spectrumSize;

    private int minShow;
    private int maxShow;

    private double max;
    private double soundSpeed;
    private int temperature;

    private double[] recordSpectrum;
    private int counter;
    private boolean measure;
    private int start;
    private double temp;

    private TextView tvText;

    private boolean btnStartStop;
    private boolean btnFreeze;

    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;

    private String[] optionTitles;

    private boolean color;
    private LinearLayout lLayout;
    private Button measureBtn;
    private Button freezeBtn;


    private class RecordThread extends Thread {
        public void run() {
            startRecording();

            while (readEnabled) {

                buffer = new byte[AudioRecord.getMinBufferSize(44100,
                        channelConfiguration,
                        audioEncoding)];
                recorder.read(buffer, 0, buffer.length);

                spectrum = fourier.doFFT(buffer);

                final LineGraph line = new LineGraph();

                if (spectrumHold != null) {
                    for (int i = 0; i < spectrumSize / 2; i++) {
                        if(spectrumHold != null) {
                            line.addNewPoints_line2((i * 22050 / fourier.getFftSize()), spectrumHold[i]);
                        }
                    }
                }

                for (int i = 0; i < spectrum.length / 2; i++) {
                    line.addNewPoints((i * 22050 / fourier.getFftSize()), spectrum[i]);
                    if (spectrumHold != null && spectrum[i] > spectrumHold[i]) {
                        spectrumHold[i] = spectrum[i];
                        line.addNewPoints_line2((i * 22050 / fourier.getFftSize()), spectrumHold[i]);
                    } else if (recordSpectrum != null && spectrum[i] > recordSpectrum[i]) {
                        recordSpectrum[i] = spectrum[i];
                    }
                }

                if (measure) {
                    if (temp == 0) {
                        temp = fourier.findMax(spectrum);
                        counter = 1;
                    } else if (counter < 3 && temp == fourier.findMax(spectrum)) {
                        counter++;
                    } else if (counter == 3) {
                        start = (int) temp;
                    } else {
                        temp = fourier.findMax(spectrum);
                        counter = 1;
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        view = line.getView(MainActivity.this);
                        line.setColor(color);
                        line.setMin(minShow);
                        line.setMax(maxShow);
                        chartContainer.removeAllViews();
                        chartContainer.addView(view);
                        max = fourier.findMax(spectrum) * 22050 / fourier.getFftSize();
                        tvText.setText("Domináns frekvencia: " + "" + Math.round(max * 100.0) / 100.0 + " Hz\n");
                    }
                });
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle("Spektrum");

        temperature = 20;

        optionTitles = getResources().getStringArray(R.array.options_array);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.left_drawer);

        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        drawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_menu_item, optionTitles));
        drawerList.setOnItemClickListener(new DrawerItemClickListener());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.drawer_open,
                R.string.drawer_close
        ) {
            public void onDrawerClosed(View view) {
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu();
            }
        };

        drawerLayout.setDrawerListener(drawerToggle);

        chartContainer = (LinearLayout) findViewById(R.id.chart);

        tvText = (TextView) findViewById(R.id.tv);
        lLayout = (LinearLayout) findViewById(R.id.buttons);
        measureBtn = (Button) findViewById(R.id.startstopbtn);
        freezeBtn = (Button) findViewById(R.id.freezebtn);

        btnStartStop = true;
        btnFreeze = true;
        color = false;
        soundSpeed = 331.5 + 0.6 * temperature;
        minShow = 0;
        maxShow = 11025;
        fourier = new FFT();
        bufferHold = null;
        spectrumHold = null;
        recordSpectrum = null;
        counter = 0;
        measure = false;
        start = 0;
        temp = 0;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putDoubleArray("spectrumHold", spectrumHold);
        savedInstanceState.putDoubleArray("recordSpectrum", recordSpectrum);
        savedInstanceState.putInt("minShow", minShow);
        savedInstanceState.putInt("maxShow", maxShow);
        savedInstanceState.putBoolean("color", color);
        savedInstanceState.putInt("temperature", temperature);
        savedInstanceState.putDouble("soundSpeed", soundSpeed);
        savedInstanceState.putInt("counter", counter);
        savedInstanceState.putInt("start", start);
        savedInstanceState.putDouble("temp", temp);
        savedInstanceState.putBoolean("measure", btnStartStop);
        savedInstanceState.putBoolean("freeze", btnFreeze);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        spectrumHold = savedInstanceState.getDoubleArray("spectrumHold");
        recordSpectrum = savedInstanceState.getDoubleArray("recordSpectrum");
        minShow = savedInstanceState.getInt("minShow");
        maxShow = savedInstanceState.getInt("maxShow");
        color = savedInstanceState.getBoolean("color");
        temperature = savedInstanceState.getInt("temperature");
        soundSpeed = savedInstanceState.getDouble("soundSpeed");
        counter = savedInstanceState.getInt("counter");
        start = savedInstanceState.getInt("start");
        temp = savedInstanceState.getDouble("temp");
        btnStartStop = savedInstanceState.getBoolean("measure");
        btnFreeze = savedInstanceState.getBoolean("freeze");
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
        if (btnStartStop) {
            bufferRecord = new byte[buffer.length];
            bufferRecord = buffer;
            recordSpectrum = fourier.doFFT(bufferRecord);
            measure = true;
            mBtn.setText("Leállítás");
            btnStartStop = false;
        } else {
            measure = false;
            Intent i = new Intent(MainActivity.this, ResultActivity.class);
            i.putExtra("buffer", recordSpectrum);
            i.putExtra("start", start);
            i.putExtra("fftSize", fourier.getFftSize());
            i.putExtra("soundSpeed", soundSpeed);
            i.putExtra("temperature", temperature);
            i.putExtra("color", color);
            startActivity(i);
            mBtn.setText("Mérés");
            btnStartStop = true;
        }
    }

    public void buttonFreeze(View viewFreeze) {
        Button freezeBtn = (Button) findViewById(R.id.freezebtn);
        if (btnFreeze) {
            bufferHold = new byte[buffer.length];
            bufferHold = buffer;
            spectrumHold = fourier.doFFT(bufferHold);
            spectrumSize = spectrumHold.length;
            freezeBtn.setText("Leállítás");
            btnFreeze = false;
        } else {
            spectrumHold = null;
            spectrumSize = 0;
            freezeBtn.setText("Csúcstartó");
            btnFreeze = true;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean drawerOpen = drawerLayout.isDrawerOpen(drawerList);
        menu.findItem(R.id.action_about).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.action_about:
                LayoutInflater inflater = this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.about, null);

                new AlertDialog.Builder(this)
                        .setView(dialogView)
                        .setTitle("Névjegy")
                        .setNeutralButton("Köszönöm!", null)
                        .setIcon(R.drawable.ic_launcher)
                        .show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        switch (position) {
            case 0:
                final AlertDialog.Builder optionsBuilder = new AlertDialog.Builder(this);
                final EditText input = new EditText(this);
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                optionsBuilder.setTitle("Hőmérséklet: " + "" + temperature + "°C");
                optionsBuilder.setView(input);
                optionsBuilder.setPositiveButton("Rendben", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            temperature = Integer.parseInt(input.getText().toString().trim());
                        } catch (Exception e) {
                            dialog.cancel();
                        }
                        soundSpeed = 331.5 + 0.6 * temperature;
                    }
                });
                optionsBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });
                optionsBuilder.show();


                break;
            case 1:
                if (color) {
                    optionTitles[position] = "Szín: negatív";
                    tvText.setTextColor(Color.parseColor("#FFFFFF"));
                    tvText.setBackgroundColor(Color.parseColor("#000000"));
                    lLayout.setBackgroundColor(Color.parseColor("#000000"));
                    measureBtn.setBackgroundColor(Color.parseColor("#000000"));
                    measureBtn.setTextColor(Color.parseColor("#FFFFFF"));
                    freezeBtn.setBackgroundColor(Color.parseColor("#000000"));
                    freezeBtn.setTextColor(Color.parseColor("#FFFFFF"));
                    color = false;
                } else {
                    optionTitles[position] = "Szín: pozitív";
                    tvText.setTextColor(Color.parseColor("#000000"));
                    tvText.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    lLayout.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    measureBtn.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    measureBtn.setTextColor(Color.parseColor("#000000"));
                    freezeBtn.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    freezeBtn.setTextColor(Color.parseColor("#000000"));
                    color = true;
                }
                break;
            case 2:
                final AlertDialog.Builder zoomMinBuilder = new AlertDialog.Builder(this);
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                View layout = inflater.inflate(R.layout.options, null);
                zoomMinBuilder.setView(layout);
                final EditText inputMin = (EditText) layout.findViewById(R.id.editTextMin);
                final EditText inputMax = (EditText) layout.findViewById(R.id.editTextMax);
                inputMin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        inputMin.setText("");
                    }
                });
                inputMax.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        inputMax.setText("");
                    }
                });
                zoomMinBuilder.setTitle("Közelítés[Hz]");
                inputMin.setText("" + minShow);
                inputMax.setText("" + maxShow);
                zoomMinBuilder.setPositiveButton("Rendben", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            if (Integer.parseInt(inputMin.getText().toString().trim()) < Integer.parseInt(inputMax.getText().toString().trim())) {
                                minShow = Integer.parseInt(inputMin.getText().toString().trim());
                                maxShow = Integer.parseInt(inputMax.getText().toString().trim());
                            } else if (inputMin.getText().toString().trim().length() > 0 && Integer.parseInt(inputMin.getText().toString().trim()) < maxShow) {
                                minShow = Integer.parseInt(inputMin.getText().toString().trim());
                            } else if (inputMax.getText().toString().trim().length() > 0 && Integer.parseInt(inputMax.getText().toString().trim()) > minShow) {
                                maxShow = Integer.parseInt(inputMax.getText().toString().trim());
                            } else {
                                dialog.cancel();
                            }
                        } catch (Exception e) {
                            dialog.cancel();
                        }
                    }
                });
                zoomMinBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });
                zoomMinBuilder.setNeutralButton("Visszaállítás", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        minShow = 0;
                        maxShow = 11025;
                    }
                });
                zoomMinBuilder.show();
                break;
            case 3:
                View messageView = getLayoutInflater().inflate(R.layout.howtouse, null, false);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setIcon(R.drawable.ic_launcher);
                builder.setTitle(R.string.app_name);
                builder.setView(messageView);
                builder.setNeutralButton("Vissza", null);
                builder.show();
                break;
        }

        drawerList.setItemChecked(position, true);
        drawerLayout.closeDrawer(drawerList);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

}


