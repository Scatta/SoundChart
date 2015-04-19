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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.achartengine.GraphicalView;


public class MainActivity extends ActionBarActivity {

    private boolean readEnabled = true;

    private static GraphicalView view;

    AudioRecord recorder = null;
    private static int audioEncoding = 2;
    private static int channelConfiguration = 16;
    private static byte[] buffer;
    private static byte[] bufferHold;
    private LinearLayout chartContainer;

    private FFT fourier;

    private double[] spectrum;
    private double[] spectrumHold;

    private int minShow;
    private int maxShow;

    private double max;
    private double maxHold;
    private double speed;
    private double soundSpeed;
    private int temperatue;

    private double[] recordBuffer;
    private int counter;
    private boolean measure;

    private TextView tvText;

    enum mEnum {Start, Stop};
    private mEnum btnStartStop;

    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;

    private String[] optionTitles;

    private boolean color;
    private LinearLayout lLayout;
    //private EditText editText;


    private class RecordThread extends Thread {
        public void run() {
            startRecording();
            fourier = new FFT();
            bufferHold = null;
            spectrumHold = null;
            recordBuffer = new double[512];
            counter = 0;
            measure = false;


            while (readEnabled) {

                buffer = new byte[AudioRecord.getMinBufferSize(44100,
                        channelConfiguration,
                        audioEncoding)];
                recorder.read(buffer, 0, buffer.length);

                spectrum = fourier.doFFT(buffer);

                final LineGraph line = new LineGraph();
                for (int i = 0; i < spectrum.length / 2; i++) {
                    line.addNewPoints((i * 22050 / fourier.getFftSize()), spectrum[i]);  //Math.log10(spectrum[i]/1000)
                }


                if (spectrumHold != null) {
                    for (int i = 0; i < spectrumHold.length / 2; i++) {
                        line.addNewPoints_line2((i * 22050 / fourier.getFftSize()), spectrumHold[i]);
                    }
                }

                if (measure) {
                    recordBuffer[counter] = fourier.findMax(spectrum) * 22050 / fourier.getFftSize();
                    if (counter != 511) {
                        counter++;
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(MainActivity.this, "test...", Toast.LENGTH_SHORT).show();
                        view = line.getView(MainActivity.this);
                        line.setColor(color);
                        line.setMin(minShow);
                        line.setMax(maxShow);
                        chartContainer.removeAllViews();
                        chartContainer.addView(view);
                        max = fourier.findMax(spectrum) * 22050 / fourier.getFftSize();
                        tvText.setText("Domináns frekvencia: " + "" + max + " Hz\n" +
                                "Referencia frekvencia: 0.0 Hz\n" +
                                "Sebesség: 0 km/h");
                        if (spectrumHold != null) {
                            maxHold = fourier.findMax(spectrumHold) * 22050 / fourier.getFftSize();
                            speed = soundSpeed * ((max / maxHold) - 1);
                            speed = speed * 3.6;
                            speed = Math.round(speed * 100.0) / 100.0;
                            tvText.setText("Domináns frekvencia: " + "" + max + " Hz\n" +
                                    "Referencia frekvencia: " + "" + maxHold + " Hz\n" +
                                    "Sebesség: " + speed + " km/h");
                        }
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

        temperatue = 20;

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
                optionTitles[0] = "Hőmérséklet: "+""+temperatue+"°C"; // ??
                invalidateOptionsMenu();
            }
        };


        drawerLayout.setDrawerListener(drawerToggle);

        chartContainer = (LinearLayout) findViewById(R.id.chart);

        tvText = (TextView) findViewById(R.id.tv);
        lLayout = (LinearLayout) findViewById(R.id.buttons);

        btnStartStop = mEnum.Start;
        color = false;
        soundSpeed = 331.5 + 0.6*temperatue;
        minShow = 0;
        maxShow = 11025;

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
                //startRecordingThread();
                measure = true;
                mBtn.setText("Leállítás");
                btnStartStop = mEnum.Stop;
                break;
            case Stop:
                //stopRecording();
                measure = false;
                Intent i = new Intent(MainActivity.this, ResultActivity.class);
                i.putExtra("buffer", recordBuffer);
                i.putExtra("soundSpeed", soundSpeed);
                i.putExtra("temperature",temperatue);
                i.putExtra("color",color);
                startActivity(i);
                mBtn.setText("Mérés");
                btnStartStop = mEnum.Start;
                break;
        }
    }

    public void buttonFreeze(View viewFreeze) {
        bufferHold = new byte[buffer.length];
        bufferHold = buffer;
        spectrumHold = fourier.doFFT(bufferHold);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = drawerLayout.isDrawerOpen(drawerList);
        menu.findItem(R.id.action_about).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
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

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        switch(position){
            case 0:
                //View optionsView = getLayoutInflater().inflate(R.layout.options, null, false);

                //editText = (EditText) findViewById(R.id.editText);


                final AlertDialog.Builder optionsBuilder = new AlertDialog.Builder(this);
                final EditText input = new EditText(this);
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                optionsBuilder.setTitle("Hőmérséklet[°C]");
                optionsBuilder.setView(input);
                optionsBuilder.setPositiveButton("Rendben", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            temperatue = Integer.parseInt(input.getText().toString().trim());
                        } catch (Exception e){
                            dialog.cancel();
                        }
                        soundSpeed = 331.5 + 0.6 * temperatue;
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
                if(color) {
                    optionTitles[position] = "Szín: negatív";
                    tvText.setTextColor(Color.parseColor("#FFFFFF"));
                    tvText.setBackgroundColor(Color.parseColor("#000000"));
                    lLayout.setBackgroundColor(Color.parseColor("#000000"));
                    color = false;
                }
                else{
                    optionTitles[position] = "Szín: pozitív";
                    tvText.setTextColor(Color.parseColor("#000000"));
                    tvText.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    lLayout.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    color = true;
                }
                break;
            case 2:
                View messageView = getLayoutInflater().inflate(R.layout.howtouse, null, false);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setIcon(R.drawable.ic_launcher);
                builder.setTitle(R.string.app_name);
                builder.setView(messageView);
                builder.setNeutralButton("Vissza", null);
                builder.show();
                break;
            case 3:
                final AlertDialog.Builder zoomMinBuilder = new AlertDialog.Builder(this);
                final EditText inputMin = new EditText(this);
                inputMin.setInputType(InputType.TYPE_CLASS_NUMBER);
                zoomMinBuilder.setTitle("Zoom: Min[Hz]");
                zoomMinBuilder.setView(inputMin);
                zoomMinBuilder.setPositiveButton("Rendben", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            minShow = Integer.parseInt(inputMin.getText().toString().trim());
                        } catch (Exception e){
                            dialog.cancel();
                        }
                    }
                });
                zoomMinBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });
                zoomMinBuilder.show();
                break;
            case 4:
                final AlertDialog.Builder zoomMaxBuilder = new AlertDialog.Builder(this);
                final EditText inputMax = new EditText(this);
                inputMax.setInputType(InputType.TYPE_CLASS_NUMBER);
                zoomMaxBuilder.setTitle("Zoom: Max[Hz]");
                zoomMaxBuilder.setView(inputMax);
                zoomMaxBuilder.setPositiveButton("Rendben", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            maxShow = Integer.parseInt(inputMax.getText().toString().trim());
                        } catch (Exception e){
                            dialog.cancel();
                        }
                    }
                });
                zoomMaxBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });
                zoomMaxBuilder.show();
                break;
            case 5:
                minShow = 0;
                maxShow = 11025;
                break;
        }

        // update selected item and title, then close the drawer
        drawerList.setItemChecked(position, true);
        drawerLayout.closeDrawer(drawerList);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        drawerToggle.onConfigurationChanged(newConfig);
    }

}


