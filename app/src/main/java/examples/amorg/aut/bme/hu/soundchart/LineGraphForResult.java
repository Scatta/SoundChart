package examples.amorg.aut.bme.hu.soundchart;

import android.content.Context;
import android.graphics.Color;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

/**
 * Created by SlimShady on 2015.04.11..
 */

public class LineGraphForResult {
    private GraphicalView view;

    private TimeSeries dataset = new TimeSeries("Eredmény");
    private TimeSeries dataset3 = new TimeSeries("Közeledő frekvencia");
    private TimeSeries dataset2 = new TimeSeries("Távolodó frekvencia");
    private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();

    private XYSeriesRenderer renderer = new XYSeriesRenderer();
    private XYSeriesRenderer renderer3 = new XYSeriesRenderer();
    private XYSeriesRenderer renderer2 = new XYSeriesRenderer();
    private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();

    public LineGraphForResult()
    {
        mDataset.addSeries(dataset);
        mDataset.addSeries(dataset2);
        mDataset.addSeries(dataset3);

        renderer.setColor(Color.WHITE);
        renderer.setChartValuesTextSize(25);

        renderer2.setColor(Color.BLUE);
        renderer2.setDisplayChartValues(false);

        renderer3.setColor(Color.RED);
        renderer3.setDisplayChartValues(false);

        mRenderer.setApplyBackgroundColor(true);
        mRenderer.setBackgroundColor(Color.BLACK);
        mRenderer.setMarginsColor(Color.BLACK);

        mRenderer.setXTitle("Frekvencia [Hz]");
        mRenderer.setYTitle("Amplitúdó");

        mRenderer.setLabelsTextSize(15);
        mRenderer.setXLabels(10);

        mRenderer.addSeriesRenderer(renderer);
        mRenderer.addSeriesRenderer(renderer2);
        mRenderer.addSeriesRenderer(renderer3);
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

    public void setFCome(double x, double max)
    {
        dataset3.add(x, 0);
        dataset3.add(x, max);
    }

    public void setFGo(double x, double max)
    {
        dataset2.add(x, 0);
        dataset2.add(x, max);
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
}