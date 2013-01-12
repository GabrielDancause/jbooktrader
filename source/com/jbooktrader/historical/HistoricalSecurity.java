package com.jbooktrader.historical;

import com.jbooktrader.historical.model.HistoricalData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Created by: marcus
 * Date: 1/9/13
 * Time: 10:50 PM
 */
public class HistoricalSecurity {

    protected TreeMap<Date,HistoricalData> dataMap;
    protected String symbol;
    protected String type;
    protected String exchange;
    protected BarSize barSize;

    protected TimeZone timezone;

    public enum BarSize {
        sec1("1 sec"),
        sec_5("5 secs"),
        sec_15("15 secs"),
        sec_30("30 secs"),
        min_1("1 min"),
        min_2("2 mins"),
        min_3("3 mins"),
        min_5("5 mins"),
        min_15("15 mins"),
        min_30("30 mins"),
        hour_1("1 hour"),
        day_1("1 day");

        private String size;

        BarSize(String s) {
            size = s;
        }

        public String getSize() {
            return size;
        }
    }

    public HistoricalSecurity(String symbol, String type, String exchange, HistoricalSecurity.BarSize barSize) {
        dataMap = new TreeMap<>();
        this.symbol = symbol;
        this.type = type;
        this.exchange = exchange;
        this.timezone = TimeZone.getTimeZone("America/New_York");    // default to this
        this.barSize = barSize;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getType() {
        return type;
    }

    public String getExchange() {
        return exchange;
    }

    public TimeZone getTimezone() {
        return timezone;
    }

    public boolean isValid(Date sStart, Date sEnd) {
           return dataMap.firstKey().getTime() <= sStart.getTime() &&
                   dataMap.lastKey().getTime() >= sEnd.getTime();
    }

    public HistoricalData getData(Date day) {
        if (dataMap == null) {
            return null;
        }
        return dataMap.get(day);

    }

    public void insertData(HistoricalData data) {
        dataMap.put(data.getDate(),data);
    }

    public void clear() {
        dataMap.clear();
    }

    public HistoricalData deleteData(Date d) {
        return dataMap.remove(d);
    }

    public int size() {
        return dataMap.size();
    }

    public SortedMap<Date,HistoricalData> getRange(Date start,boolean sinc, Date end, boolean einc) {
        return dataMap.subMap(start,sinc,end,einc);
    }

    public NavigableMap<Date,HistoricalData> getDescendingMap() {
        return dataMap.descendingMap();
    }

    public TreeMap<Date,HistoricalData> getDatMap() {
        return dataMap;
    }

    public BarSize getBarSize() {
        return barSize;
    }
}
