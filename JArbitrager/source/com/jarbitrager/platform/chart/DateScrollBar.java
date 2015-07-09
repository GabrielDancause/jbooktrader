package com.jarbitrager.platform.chart;

import org.jfree.chart.axis.*;
import org.jfree.chart.event.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.*;
import org.jfree.data.xy.*;

import javax.swing.*;
import java.awt.event.*;
import java.util.*;

/**
 * Scroll bar for a combined chart where the horizontal axis represents dates
 */
public class DateScrollBar extends JScrollBar implements AdjustmentListener, AxisChangeListener {
    private static final long SCALER = 10000;
    private final DateAxis dateAxis;
    private final CombinedDomainXYPlot combinedDomainPlot;

    public DateScrollBar(CombinedDomainXYPlot combinedDomainPlot) {

        super(HORIZONTAL);
        this.combinedDomainPlot = combinedDomainPlot;

        dateAxis = (DateAxis) combinedDomainPlot.getDomainAxis();
        int min = (int) (dateAxis.getLowerBound() / SCALER);
        int max = (int) (dateAxis.getUpperBound() / SCALER);
        setValues(max, 0, min, max);

        dateAxis.addChangeListener(this);
        addAdjustmentListener(this);
    }

    private void rangeUpdate() {
        List<?> subPlots = combinedDomainPlot.getSubplots();
        double lowerBound = dateAxis.getLowerBound();
        double upperBound = dateAxis.getUpperBound();

        if (lowerBound >= upperBound) {
            return;
        }

        for (Object subPlot : subPlots) {
            XYPlot plot = (XYPlot) subPlot;
            int datasetCount = plot.getDatasetCount();
            for (int datasetNumber = 0; datasetNumber < datasetCount; datasetNumber++) {
                double min = Double.MAX_VALUE;
                double max = Double.MIN_VALUE;

                XYDataset dataset = plot.getDataset(datasetNumber);
                int seriesCount = dataset.getSeriesCount();

                for (int series = 0; series < seriesCount; series++) {
                    int[] itemBounds = RendererUtilities.findLiveItems(dataset, series, lowerBound, upperBound);
                    int firstItem = itemBounds[0];
                    int lastItem = itemBounds[1];

                    for (int item = firstItem; item < lastItem; item++) {
                        double value = dataset.getYValue(series, item);
                        max = Math.max(value, max);
                        min = Math.min(value, min);
                    }
                }

                if (max > min) {
                    plot.getRangeAxis(datasetNumber).setRange(min, max);
                }
            }
        }
    }


    public void axisChanged(AxisChangeEvent event) {
        rangeUpdate();
        int value = (int) (dateAxis.getUpperBound() / SCALER);
        setValue(value);
    }

    public void adjustmentValueChanged(AdjustmentEvent e) {
        double end = getValue() * SCALER;
        double start = end - dateAxis.getRange().getLength();
        if (start < end) {
            dateAxis.setRange(start, end);
        }
    }
}
