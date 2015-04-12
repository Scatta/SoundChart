package examples.amorg.aut.bme.hu.soundchart;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Intent intent = getIntent();

        double[] buffer = intent.getDoubleArrayExtra("buffer");

        buffer = deleteZeros(buffer);

        tvResults = (TextView) findViewById(R.id.tvResult);
        tvSpeed = (TextView) findViewById(R.id.tvSpeed);

        for(int i = 0; i < buffer.length; i++){
            tvResults.append(""+buffer[i]+"\n");
        }

        tvResults.append("Méret: "+""+buffer.length);

        range = 300;
        average = firstTenAverage(buffer);

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

        speed = 344*((fCome-fGo)/(fCome+fGo));

        speed = speed * 3.6 ;

        speed = Math.round(speed*100.0)/100.0;

        tvSpeed.setText("A jármű sebessége: "+""+Math.round(speed)+" km/h "+"("+""+speed+")");

        chartContainer = (LinearLayout) findViewById(R.id.chartResult);

        final LineGraphForResult line = new LineGraphForResult();
        for (int i = 0; i < buffer.length; i++) {
            line.addNewPoints(i , buffer[i]);
        }

        view = line.getView(ResultActivity.this);
        line.setMin(average-range);
        line.setMax(average+range);
        chartContainer.removeAllViews();
        chartContainer.addView(view);

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



}
