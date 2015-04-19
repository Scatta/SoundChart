package examples.amorg.aut.bme.hu.soundchart;

import android.content.Context;
import android.graphics.Color;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

/**
 * Created by SlimShady on 2015.03.28..
 */
public class LineGraph {
    private GraphicalView view;

    private TimeSeries dataset = new TimeSeries("Spectrum");
    private TimeSeries dataset2 = new TimeSeries("Referece");
    private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();

    private XYSeriesRenderer renderer = new XYSeriesRenderer();
    private XYSeriesRenderer renderer2 = new XYSeriesRenderer();
    private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();


    //private double min=0;
    //private double max=11025;


    public LineGraph()
    {
        mDataset.addSeries(dataset);
        mDataset.addSeries(dataset2);

        renderer.setColor(Color.WHITE);
        renderer.setChartValuesTextSize(25);

        renderer2.setColor(Color.GREEN);
        renderer2.setDisplayChartValues(false);

        mRenderer.setApplyBackgroundColor(true);
        mRenderer.setBackgroundColor(Color.BLACK);
        mRenderer.setMarginsColor(Color.BLACK);

        mRenderer.setXTitle("Frequency [Hz]");
        mRenderer.setYTitle("Magnitude");

        mRenderer.setLabelsTextSize(15);
        mRenderer.setXLabels(10);

        //mRenderer.setZoomButtonsVisible(true);
        //mRenderer.setZoomEnabled(true);
        //mRenderer.setZoomRate(10);
        //mRenderer.setPanEnabled(true,false);
        //mRenderer.setXAxisMin(min);
        //mRenderer.setXAxisMax(max);

        mRenderer.addSeriesRenderer(renderer);
        mRenderer.addSeriesRenderer(renderer2);
    };

    public GraphicalView getView(Context context)
    {
        view =  ChartFactory.getLineChartView(context, mDataset, mRenderer);
        return view;
    }

    public void addNewPoints(double x, double y)
    {
        dataset.add(x, y);
    }

    public void addNewPoints_line2(double x, double y)
    {
        dataset2.add(x, y);
    }

    public void setMin(double reference){
        mRenderer.setXAxisMin(reference);
    };

    public void setMax(double reference){
        mRenderer.setXAxisMax(reference);
    };

    public void setColor(boolean invert){
        if(invert == true) {
            renderer.setColor(Color.BLACK);
            mRenderer.setBackgroundColor(Color.WHITE);
            mRenderer.setMarginsColor(Color.WHITE);
        }
        else{
            renderer.setColor(Color.WHITE);
            mRenderer.setBackgroundColor(Color.BLACK);
            mRenderer.setMarginsColor(Color.BLACK);
        }
    }
/*
    public void setMin(double reference){
        min = reference - 1000;
    };

    public void setMax(double reference){
        max = reference + 1000;
    };
*/
}
