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

    private TimeSeries dataset = new TimeSeries("Frequency Results");
    private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();

    private XYSeriesRenderer renderer = new XYSeriesRenderer();
    private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();

    public LineGraphForResult()
    {
        mDataset.addSeries(dataset);

        renderer.setColor(Color.WHITE);
        renderer.setChartValuesTextSize(25);

        mRenderer.setApplyBackgroundColor(true);
        mRenderer.setBackgroundColor(Color.BLACK);
        mRenderer.setMarginsColor(Color.BLACK);

        mRenderer.setXTitle("Time");
        mRenderer.setYTitle("Frequency [Hz]");

        mRenderer.setLabelsTextSize(15);
        mRenderer.setXLabels(10);

        mRenderer.addSeriesRenderer(renderer);
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

    public void setMin(double reference){
        mRenderer.setYAxisMin(reference);
    };

    public void setMax(double reference){
        mRenderer.setYAxisMax(reference);
    };
}