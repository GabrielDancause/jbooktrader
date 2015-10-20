package com.jarbitrager.platform.chart;

import com.jarbitrager.platform.indicator.*;
import com.jarbitrager.platform.instrument.*;
import com.jarbitrager.platform.position.*;
import com.jarbitrager.platform.util.*;
import org.jfree.data.time.*;

import java.util.*;

/**
 * Encapsulates performance chart data.
 */
public class PerformanceChartData {
    private final TimeSeries netProfit, prices1, prices2;
    private final Map<String, TimeSeries> indicators;
    private final ArrayList<CircledTextAnnotation> annotations;

    public PerformanceChartData(String symbol1, String symbol2) {
        netProfit = new TimeSeries("Net Profit");
        prices1 = new TimeSeries(symbol1);
        prices2 = new TimeSeries(symbol2);
        indicators = new HashMap<String, TimeSeries>();
        annotations = new ArrayList<CircledTextAnnotation>();
    }

    public ArrayList<CircledTextAnnotation> getAnnotations() {
        return annotations;
    }

    public void updateAnnotations(Position position) {
        long time = position.getTime();
        double aveFill = position.getAvgFillPrice();
        int quantity = position.getPosition();
        CircledTextAnnotation trade = new CircledTextAnnotation(quantity, time, aveFill);
        annotations.add(trade);
    }

    public TimeSeries getPrices1() {
        return prices1;
    }

    public TimeSeries getPrices2() {
        return prices2;
    }

    public boolean isEmpty() {
        return (prices1.isEmpty() || prices2.isEmpty());
    }

    public void addIndicator(Indicator indicator) {
        indicators.put(indicator.getName(), new TimeSeries(indicator.getName()));
    }

    public void updateNetProfit(TimedValue profitAndLoss) {
        netProfit.addOrUpdate(new Second(new Date(profitAndLoss.getTime())), profitAndLoss.getValue());
    }

    public TimeSeries getProfitAndLossSeries() {
        return netProfit;
    }

    public void updateIndicators(List<Indicator> indicatorsToUpdate, long time) {
        Second second = new Second(new Date(time));
        for (Indicator indicator : indicatorsToUpdate) {
            TimeSeries indicatorSeries = indicators.get(indicator.getName());
            indicatorSeries.addOrUpdate(second, indicator.getValue());
        }
    }

    public void updatePrices(MarketSnapshot marketSnapshot) {
        Second second = new Second(new Date(marketSnapshot.getTime()));
        prices1.addOrUpdate(second, marketSnapshot.getMidPrice1());
        prices2.addOrUpdate(second, marketSnapshot.getMidPrice2());
    }


    public TimeSeries getPriceDataset1() {
        return prices1;
    }

    public TimeSeries getPriceDataset2() {
        return prices2;
    }


    public TimeSeries getIndicatorDataset(Indicator indicator) {
        return indicators.get(indicator.getName());
    }

}
