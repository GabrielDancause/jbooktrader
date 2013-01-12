package com.jbooktrader.historical.impl;

import com.jbooktrader.historical.HistoricalSecurity;
import com.jbooktrader.historical.IHistoricalSecurityFactory;
import com.jbooktrader.platform.model.Dispatcher;
import com.jbooktrader.platform.report.EventReport;
import com.jbooktrader.platform.startup.JBookTrader;

import java.io.IOException;

/**
 * Created by: marcus
 * Date: 1/11/13
 * Time: 3:42 AM
 */
public class HistoricalSecurityFactory implements IHistoricalSecurityFactory {

    private static final String FILE_SEP = System.getProperty("file.separator");
    private static final String LINE_SEP = System.getProperty("line.separator");

    private String path;
    private final Dispatcher dispatcher;
    private final EventReport eventReport;


    public HistoricalSecurityFactory(String path) {
        this.path = path;
        dispatcher = Dispatcher.getInstance();
        eventReport = dispatcher.getEventReport();
    }

    @Override
    public HistoricalSecurity getInstance(String symbol, String type, String exchange, HistoricalSecurity.BarSize barSize) {
        HistoricalSecurity sec = null;
        try {
            String filepath = path+FILE_SEP+symbol+"-"+exchange+"-"+type+"."+"txt";
            sec = new FileBackedHistoricalSecurity(filepath, symbol, type, exchange, barSize);
        } catch (IOException e) {
            eventReport.report(JBookTrader.APP_NAME, "Can't open file associated with Historical data, not loaded " + symbol + "-" + exchange + "-" + type);
        }
        return sec; // may return null if we throw exception, it means we couldn't create the security
    }
}
