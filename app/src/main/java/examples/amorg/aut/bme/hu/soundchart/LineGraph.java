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

    private TimeSeries dataset = new TimeSeries("SoundWave");
    private TimeSeries dataset2 = new TimeSeries("Referece");
    private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();

    private XYSeriesRenderer renderer = new XYSeriesRenderer();
    private XYSeriesRenderer renderer2 = new XYSeriesRenderer();
    private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();


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

}
