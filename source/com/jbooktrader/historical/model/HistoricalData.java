package com.jbooktrader.historical.model;

import java.util.Date;

/**
 * User: marcus
 * Date: 1/9/13
 * Time: 2:47 PM
 *
 *
 * Model object which represents a single day in historical data
 * immutable
 */
public final class HistoricalData {

    private final Date date;
    private final double open;
    private final double high;
    private final double low;
    private final double close;
    private final int volume;
    private final boolean hasGaps;

    public HistoricalData(Date date, double open, double high, double low, double close, int volume, boolean hasGaps) {
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.hasGaps = hasGaps;
    }

    public Date getDate() {
        return new Date(date.getTime());
    }

    public double getOpen() {
        return open;
    }

    public double getHigh() {
        return high;
    }

    public double getLow() {
        return low;
    }

    public double getClose() {
        return close;
    }

    public int getVolume() {
        return volume;
    }

    public Boolean getHasGaps() {
        return hasGaps;
    }
}
