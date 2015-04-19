package examples.amorg.aut.bme.hu.soundchart;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.achartengine.GraphicalView;

import java.text.DecimalFormat;


public class ResultActivity extends ActionBarActivity {

    private double[] buffer;
    private TextView tvResults;
    private TextView tvSpeed;
    private static GraphicalView view;
    private LinearLayout chartContainer;
    private double range;
    private double average;
    private double speed;
    private double fCome;
    private double fGo;
    private double temp;
    private int seged;
    private boolean color;
    private int temperatue;
    private double soundSpeed;
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;
    private String[] optionTitles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        getSupportActionBar().setTitle("Eredmények");

        optionTitles = getResources().getStringArray(R.array.options_array_results);
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

        soundSpeed = 344;
        temperatue = 20;
        color = false;

        Intent intent = getIntent();
        color = intent.getBooleanExtra("color",color);
        temperatue = intent.getIntExtra("temperature",temperatue);
        soundSpeed = intent.getDoubleExtra("soundSpeed",soundSpeed);
        buffer = intent.getDoubleArrayExtra("buffer");

        buffer = deleteZeros(buffer);

        tvResults = (TextView) findViewById(R.id.tvResult);
        tvSpeed = (TextView) findViewById(R.id.tvSpeed);

        for(int i = 0; i < buffer.length; i++){
            tvResults.append(""+buffer[i]+"\n");
        }

        tvResults.append("Méret: "+""+buffer.length);

        range = 300;
        if(buffer.length<10){
            Toast.makeText(this, "Túl kevés adat.", Toast.LENGTH_SHORT).show();
        }
        else{
            average = firstTenAverage(buffer);
        }

        for (int i = 0; i < buffer.length; i++) {
            if(buffer[i]<(average-range) || buffer[i]>(average+range)){
                buffer[i] = 0;
            }
        }

        buffer = deleteZeros(buffer);

        fCome = average;
        fGo = average;
        seged = 0;

        for (int i = 0; i < buffer.length; i++) {
            if(buffer[i]<average && seged==0){
                temp = buffer[i];
                seged++;
            }
            else if(buffer[i]<average && seged<5){
                seged++;
            }
            else if(seged==5){
                fGo=temp;
            }
        }

        speed = soundSpeed*((fCome-fGo)/(fCome+fGo));

        speed = speed * 3.6 ;

        speed = Math.round(speed*100.0)/100.0;

        tvSpeed.setText("A jármű sebessége: "+""+Math.round(speed)+" km/h "+"("+""+speed+")");

        chartContainer = (LinearLayout) findViewById(R.id.chartResult);

        final LineGraphForResult line = new LineGraphForResult();
        for (int i = 0; i < buffer.length; i++) {
            line.addNewPoints(i , buffer[i]);
        }

        view = line.getView(ResultActivity.this);
        line.setColor(color);
        line.setMin(average-range);
        line.setMax(average+range);
        chartContainer.removeAllViews();
        chartContainer.addView(view);

        if(color==false) {
            tvSpeed.setTextColor(Color.parseColor("#FFFFFF"));
            tvSpeed.setBackgroundColor(Color.parseColor("#000000"));
            tvResults.setTextColor(Color.parseColor("#FFFFFF"));
            tvResults.setBackgroundColor(Color.parseColor("#000000"));
        }
        else{
            tvSpeed.setTextColor(Color.parseColor("#000000"));
            tvSpeed.setBackgroundColor(Color.parseColor("#FFFFFF"));
            tvResults.setTextColor(Color.parseColor("#000000"));
            tvResults.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }

    }

    private double[] deleteZeros(double[] data){
        int j = 0;
        for( int i=0;  i<data.length;  i++ )
        {
            if (data[i] != 0)
                j++;
        }
        double[] temp = new double[j];
        j=0;
        for( int i=0;  i<temp.length;  i++ )
        {
            if (data[i] != 0){
                temp[j] = data[i];
                j++;
            }
        }
        return temp;
    }

    private double firstTenAverage(double[] data){
        double avg = 0;
        for(int i = 0; i<10; i++){
            avg = avg + data[i];
        }
        avg = avg / 10;
        return avg;
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
                View messageView = getLayoutInflater().inflate(R.layout.howtouse, null, false);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setIcon(R.drawable.ic_launcher);
                builder.setTitle(R.string.app_name);
                builder.setView(messageView);
                builder.setNeutralButton("Vissza", null);
                builder.show();
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
