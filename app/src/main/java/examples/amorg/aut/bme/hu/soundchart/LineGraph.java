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
    private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();

    private XYSeriesRenderer renderer = new XYSeriesRenderer();
    private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();

    public LineGraph()
    {
        mDataset.addSeries(dataset);

        renderer.setColor(Color.BLACK);

        mRenderer.setZoomButtonsVisible(true);
        mRenderer.setXTitle("Time");
        mRenderer.setYTitle("Amplitude");

        mRenderer.addSeriesRenderer(renderer);
    }

    public GraphicalView getView(Context context)
    {
        view =  ChartFactory.getLineChartView(context, mDataset, mRenderer);
        return view;
    }

    public void addNewPoints(double x, int y)
    {
        dataset.add(x, y);
    }

    public void reset(){
        mDataset.clear();
        mDataset.addSeries(dataset);
    }

    public void setSeries(XYSeries n){
        mDataset.clear();
        mDataset.addSeries(n);
    }

}
