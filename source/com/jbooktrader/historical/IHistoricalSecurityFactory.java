package com.jbooktrader.historical;

/**
 * Created by: marcus
 * Date: 1/11/13
 * Time: 3:26 AM
 */
public interface IHistoricalSecurityFactory {
    public HistoricalSecurity getInstance(String symbol, String type, String exchange, HistoricalSecurity.BarSize barSize);
}
