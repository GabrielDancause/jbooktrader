package com.jbooktrader.historical.model;

import java.util.Date;

/**
 * User: marcus
 * Date: 1/9/13
 * Time: 2:47 PM
 *
 *
 * Model object which represents a single day in historical data
 */
public final class HistoricalData {

    private final Date date;
    private final int open;
    private final int high;
    private final int low;
    private final int close;
    private final int volume;
    private final Boolean hasGaps;

    public HistoricalData(Date date, int open, int high, int low, int close, int volume, Boolean hasGaps) {
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

    public int getOpen() {
        return open;
    }

    public int getHigh() {
        return high;
    }

    public int getLow() {
        return low;
    }

    public int getClose() {
        return close;
    }

    public int getVolume() {
        return volume;
    }

    public Boolean getHasGaps() {
        return hasGaps;
    }
}
