package com.jbooktrader.historical.indicator;

import com.jbooktrader.historical.HistoricalSecurity;
import com.jbooktrader.historical.model.HistoricalData;
import com.jbooktrader.platform.indicator.Indicator;
import com.jbooktrader.platform.model.Dispatcher;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by: marcus
 * Date: 1/11/13
 * Time: 4:16 AM
 */
public class HistoricalEMA extends Indicator {
    private static final Logger LOGGER = Logger.getLogger(HistoricalEMA.class.getName());
    private static final long DAY_IN_MS = 1000 * 60 * 60 * 24;

    private final double alpha;
    private boolean calcNeeded;
    private HistoricalSecurity historicalSecurity;
    private final int period;
    private Date latestDate; // latest date we have used in our calculation
    private int samples;
    private boolean valid;

    public HistoricalEMA(int period, String symbol, String type, String exchange) {    // period of days
        super(period);
        alpha = 2.0 / (period + 1.0);
        this.period = period;
        calcNeeded = true;
        valid = false;
        historicalSecurity = Dispatcher.getInstance().getHistoricalSecurityService().getHistoricalSecurity(symbol,type,exchange, HistoricalSecurity.BarSize.day_1);
    }

    @Override
    public void reset() {
        //value = 0.0; // don't reset on gap for HistoricalEMA
    }

    public HistoricalSecurity getHistoricalSecurity() {
        return historicalSecurity;
    }

    private Date getTodayEarliestTime() {
        long now = marketBook.getSnapshot().getTime();   // use this time, so optimization works
        Calendar cnow = Calendar.getInstance();
        cnow.setTimeZone(marketBook.getTimeZone());
        cnow.setTimeInMillis(now);
        cnow.set(Calendar.HOUR,0);
        cnow.set(Calendar.MINUTE,0);
        cnow.set(Calendar.MILLISECOND,0);
        now = cnow.getTimeInMillis(); // looking for prior day, so this is the earliest of today
        return cnow.getTime();
    }

    public void calculateInitial(Date now) {
        reset();
        NavigableMap<Date,HistoricalData> map = historicalSecurity.getDatMap();

        for (Map.Entry<Date,HistoricalData> entry: map.entrySet()) {
            if (entry.getKey().getTime() > now.getTime()) {
                break;
            }
            double price = entry.getValue().getClose();
            value += alpha * (price - value);
            samples++;
            latestDate = entry.getKey();
            LOGGER.log(Level.FINE,String.format("%f, %f",price, value));
        }
    }

    private boolean needNewSample() {
         return (getTodayEarliestTime().getTime() - latestDate.getTime() > DAY_IN_MS);
    }

    private void applySamplesInRange(Date from, Date to) {
        SortedMap<Date,HistoricalData> map = historicalSecurity.getRange(from,false,to,false);

        for(Map.Entry<Date,HistoricalData> entry: map.entrySet()) {  // all should be applied, as range is correct
            double price = entry.getValue().getClose();
            value += alpha * (price - value);
            samples++;
            latestDate = entry.getKey();
            LOGGER.log(Level.FINE,String.format("%f, %f",price, value));
        }
    }

    @Override
    public void calculate() {

        Date now = getTodayEarliestTime();

        if (calcNeeded) {  // full recalculation of EMA, first time only
            calculateInitial(now);
            calcNeeded = false;
        }

        if (needNewSample()) {   // this might run a lot of we don't have enough data, consider peeking at last sample first a a cheaper test
            applySamplesInRange(latestDate,now);
        }
    }

    public boolean isValid() {
         return (!calcNeeded && !needNewSample() && samples >= period);
    }
}
