package com.jarbitrager.platform.chart;

import com.jarbitrager.platform.indicator.*;
import static com.jarbitrager.platform.preferences.JArbitragerPreferences.*;
import com.jarbitrager.platform.preferences.*;
import com.jarbitrager.platform.strategy.*;
import com.jarbitrager.platform.util.*;
import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.time.*;
import org.jfree.ui.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Multi-plot strategy performance chart which combines price, indicators,
 * executions, and net profit.
 */

public class PerformanceChart {
    private static final int PRICE_PLOT_WEIGHT = 3;
    private static final Paint BACKGROUND_COLOR = new GradientPaint(0, 0, new Color(0, 0, 176), 0, 0, Color.BLACK);

    private final Strategy strategy;
    private final PreferencesHolder prefs;
    private final List<XYPlot> indicatorPlots;
    private final PerformanceChartData performanceChartData;

    private JFreeChart chart;
    private JFrame chartFrame;
    private CombinedDomainXYPlot combinedPlot;
    private DateAxis dateAxis;
    private XYPlot pricePlot, pnlPlot;
    private JComboBox timeLineCombo, timeZoneCombo;
    private JCheckBox indicatorVisibilityCheck, tradesVisibilityCheck, pnlVisibilityCheck;

    public PerformanceChart(JFrame parent, Strategy strategy) {
        indicatorPlots = new ArrayList<XYPlot>();
        performanceChartData = strategy.getPerformanceChartData();
        prefs = PreferencesHolder.getInstance();
        this.strategy = strategy;
        createChartFrame(parent);
        registerListeners();
    }

    private void setTimeline() {
        int timeLineType = timeLineCombo.getSelectedIndex();
        MarketTimeLine mtl = new MarketTimeLine(strategy);
        SegmentedTimeline segmentedTimeline = (timeLineType == 0) ? mtl.getAllHours() : mtl.getNormalHours();
        dateAxis.setTimeline(segmentedTimeline);
    }

    private void setTimeZone() {
        int timeZoneType = timeZoneCombo.getSelectedIndex();
        TimeZone tz = (timeZoneType == 0) ? strategy.getTradingSchedule().getTimeZone() : TimeZone.getDefault();
        dateAxis.setTimeZone(tz);
    }

    private void registerListeners() {
        timeLineCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setTimeline();
            }
        });

        timeZoneCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setTimeZone();
            }
        });

        indicatorVisibilityCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (indicatorVisibilityCheck.isSelected()) {
                    if (pnlVisibilityCheck.isSelected()) {
                        combinedPlot.remove(pnlPlot);
                    }
                    for (XYPlot plot : indicatorPlots) {
                        combinedPlot.add(plot);
                    }
                    if (pnlVisibilityCheck.isSelected()) {
                        combinedPlot.add(pnlPlot);
                    }
                } else {
                    for (XYPlot plot : indicatorPlots) {
                        combinedPlot.remove(plot);
                    }
                }
            }
        });

        pnlVisibilityCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (pnlVisibilityCheck.isSelected()) {
                    combinedPlot.add(pnlPlot);
                } else {
                    combinedPlot.remove(pnlPlot);
                }
            }
        });

        tradesVisibilityCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean show = tradesVisibilityCheck.isSelected();
                for (CircledTextAnnotation annotation : performanceChartData.getAnnotations()) {
                    if (show) {
                        pricePlot.addAnnotation(annotation);
                    } else {
                        pricePlot.removeAnnotation(annotation);
                    }
                }
            }
        });

        chartFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                prefs.set(PerformanceChartWidth, chartFrame.getWidth());
                prefs.set(PerformanceChartHeight, chartFrame.getHeight());
                prefs.set(PerformanceChartX, chartFrame.getX());
                prefs.set(PerformanceChartY, chartFrame.getY());
                prefs.set(PerformanceChartState, chartFrame.getExtendedState());
            }
        });

    }

    private void createChartFrame(JFrame parent) {
        chartFrame = new JFrame("Strategy Performance Chart - " + strategy);
        chartFrame.setIconImage(parent.getIconImage());
        chartFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel chartOptionsPanel = new JPanel(new SpringLayout());
        Border etchedBorder = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        TitledBorder chartOptionsBorder = BorderFactory.createTitledBorder(etchedBorder, "Chart Options");
        chartOptionsPanel.setBorder(chartOptionsBorder);

        JLabel timeLineLabel = new JLabel("Timeline:", JLabel.TRAILING);
        timeLineCombo = new JComboBox(new String[] {"All Hours", "Trading Hours"});
        timeLineLabel.setLabelFor(timeLineCombo);

        JLabel timeZoneLabel = new JLabel("Time Zone:", JLabel.TRAILING);
        timeZoneCombo = new JComboBox(new String[] {"Exchange", "Local"});
        timeZoneLabel.setLabelFor(timeZoneCombo);

        JLabel visibilityLabel = new JLabel("Show:");
        indicatorVisibilityCheck = new JCheckBox("Indicators", true);
        tradesVisibilityCheck = new JCheckBox("Trades", true);
        pnlVisibilityCheck = new JCheckBox("Net Profit", true);

        chartOptionsPanel.add(timeLineLabel);
        chartOptionsPanel.add(timeLineCombo);
        chartOptionsPanel.add(timeZoneLabel);
        chartOptionsPanel.add(timeZoneCombo);
        chartOptionsPanel.add(visibilityLabel);
        chartOptionsPanel.add(tradesVisibilityCheck);
        chartOptionsPanel.add(indicatorVisibilityCheck);
        chartOptionsPanel.add(pnlVisibilityCheck);

        SpringUtilities.makeOneLineGrid(chartOptionsPanel);
        JPanel northPanel = new JPanel(new SpringLayout());
        northPanel.add(chartOptionsPanel);
        SpringUtilities.makeTopOneLineGrid(northPanel);

        JPanel centerPanel = new JPanel(new SpringLayout());

        JPanel chartPanel = new JPanel(new BorderLayout());
        TitledBorder chartBorder = BorderFactory.createTitledBorder(etchedBorder, "Performance Chart");
        chartPanel.setBorder(chartBorder);

        JPanel scrollBarPanel = new JPanel(new BorderLayout());
        createChart();
        DateScrollBar dateScrollBar = new DateScrollBar(combinedPlot);
        scrollBarPanel.add(dateScrollBar);

        ChartMonitor chartMonitor = new ChartMonitor(chart);

        chartMonitor.setRangeZoomable(false);

        chartPanel.add(chartMonitor, BorderLayout.CENTER);
        chartPanel.add(scrollBarPanel, BorderLayout.SOUTH);

        centerPanel.add(chartPanel);
        SpringUtilities.makeOneLineGrid(centerPanel);

        Container contentPane = chartFrame.getContentPane();
        contentPane.add(northPanel, BorderLayout.NORTH);
        contentPane.add(centerPanel, BorderLayout.CENTER);
        chartFrame.pack();

        RefineryUtilities.centerFrameOnScreen(chartFrame);

        int chartWidth = prefs.getInt(PerformanceChartWidth);
        int chartHeight = prefs.getInt(PerformanceChartHeight);
        int chartX = prefs.getInt(PerformanceChartX);
        int chartY = prefs.getInt(PerformanceChartY);
        int chartState = prefs.getInt(PerformanceChartState);

        if (chartX >= 0 && chartY >= 0 && chartHeight > 0 && chartWidth > 0) {
            chartFrame.setBounds(chartX, chartY, chartWidth, chartHeight);
        }
        if (chartState >= 0) {
            chartFrame.setExtendedState(chartState);
        }
    }


    private void createChart() {
        dateAxis = new DateAxis();
        long start = System.currentTimeMillis();

        // parent plot
        combinedPlot = new CombinedDomainXYPlot(dateAxis);
        combinedPlot.setGap(10.0);
        combinedPlot.setOrientation(PlotOrientation.VERTICAL);

        // price plot
        TimeSeries prices1 = performanceChartData.getPriceDataset1();
        TimeSeries prices2 = performanceChartData.getPriceDataset2();
        TimeSeriesCollection prices1Collection = new TimeSeriesCollection(prices1);
        TimeSeriesCollection prices2Collection = new TimeSeriesCollection(prices2);
        NumberAxis priceAxis1 = new NumberAxis((String) prices1.getKey());
        NumberAxis priceAxis2 = new NumberAxis((String) prices2.getKey());
        priceAxis1.setAutoRangeIncludesZero(false);
        priceAxis2.setAutoRangeIncludesZero(false);

        pricePlot = new XYPlot();
        pricePlot.setDomainAxis(dateAxis);
        pricePlot.setRangeAxes(new ValueAxis[] {priceAxis1, priceAxis2});
        pricePlot.setRenderer(0, new StandardXYItemRenderer());
        pricePlot.setRenderer(1, new StandardXYItemRenderer());
        pricePlot.setDataset(0, prices1Collection);
        pricePlot.setDataset(1, prices2Collection);
        pricePlot.mapDatasetToRangeAxis(1, 1);
        pricePlot.setBackgroundPaint(BACKGROUND_COLOR);
        combinedPlot.add(pricePlot, PRICE_PLOT_WEIGHT);

        // indicator plots
        for (Indicator indicator : strategy.getIndicatorManager().getIndicators()) {
            TimeSeriesCollection indicatorCollection = new TimeSeriesCollection(performanceChartData.getIndicatorDataset(indicator));
            NumberAxis indicatorAxis = new NumberAxis(indicator.getName());
            indicatorAxis.setLabelFont(new Font("Arial Narrow", Font.PLAIN, 11));
            XYPlot indicatorPlot = new XYPlot(indicatorCollection, dateAxis, indicatorAxis, new StandardXYItemRenderer());
            indicatorPlot.setBackgroundPaint(BACKGROUND_COLOR);
            combinedPlot.add(indicatorPlot);
            indicatorPlots.add(indicatorPlot);
        }

        // positions plot
        for (CircledTextAnnotation position : performanceChartData.getAnnotations()) {
            pricePlot.addAnnotation(position);
        }

        // Net profit plot
        TimeSeriesCollection profitAndLossCollection = new TimeSeriesCollection(performanceChartData.getProfitAndLossSeries());
        NumberAxis pnlAxis = new NumberAxis("Net Profit");
        pnlPlot = new XYPlot(profitAndLossCollection, dateAxis, pnlAxis, new StandardXYItemRenderer());
        pnlPlot.setBackgroundPaint(BACKGROUND_COLOR);
        combinedPlot.add(pnlPlot);

        combinedPlot.setDomainAxis(dateAxis);

        setTimeline();
        setTimeZone();

        // Finally, create the chart
        chart = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT, combinedPlot, true);
        chart.getLegend().setPosition(RectangleEdge.TOP);
        long end = System.currentTimeMillis();
        System.out.println("total time: " + (end - start));
    }

    public JFrame getChart() {
        return chartFrame;
    }

}
