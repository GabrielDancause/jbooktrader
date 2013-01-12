package com.jbooktrader.historical.impl;

import au.com.bytecode.opencsv.CSVReader;
import com.jbooktrader.historical.HistoricalSecurity;
import com.jbooktrader.historical.model.HistoricalData;
import com.sun.deploy.uitoolkit.ui.LoggerConsole;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by: marcus
 * Date: 1/10/13
 * Time: 1:03 PM
 */
public class FileBackedHistoricalSecurity extends HistoricalSecurity {
    protected String filePath;

    public static final int DATE = 0;
    public static final int OPEN = 1;
    public static final int CLOSE = 2;
    public static final int HIGH = 3;
    public static final int LOW = 4;
    public static final int VOLUME = 5;

    private static final Logger LOGGER = Logger.getLogger(FileBackedHistoricalSecurity.class.getName());

    private SimpleDateFormat formatter;


    public FileBackedHistoricalSecurity(String filePath,String symbol, String type, String exchange, HistoricalSecurity.BarSize barSize) throws IOException {
        super(symbol, type, exchange, barSize);
        this.filePath = filePath;
        formatter = new SimpleDateFormat("MMddyy"); // ok, we go back to only year 2k here

        loadFromFile(filePath);
    }

    private void loadFromFile(String filePath) throws IOException {
        FileReader fr = new FileReader(filePath);
        CSVReader reader = new CSVReader(fr);
        String[] nextline;
        while ((nextline = reader.readNext()) != null) {
            if (nextline[DATE].startsWith("timeZone=")) {
                parseTimeZone(nextline[DATE]);
            }
            if (!(nextline[DATE].startsWith("#") || nextline[DATE].length() == 0 || nextline.length < 5)) {
                    processLine(nextline);
            }
        }

    }

    private void parseTimeZone(String tz) {
        int idx = tz.indexOf('=');
        String ourTZ = tz.substring(idx+1);
        this.timezone = TimeZone.getTimeZone(ourTZ);
        formatter.setTimeZone(timezone);
    }

    private void processLine(String[] line) {

        String sDate = line[DATE];
        String sOpen = line[OPEN];
        String sClose = line[CLOSE];
        String sHigh = line[HIGH];
        String sLow = line[LOW];
        String sVolume = line[VOLUME];


        Date date;
        double open;  // using double here, can lead to iteration rounding errors, it is smaller than BigDecimal, and follows existing project datatypes
        double close;
        double high;
        double low;
        int volume;

        HistoricalData hd = null;

        try {
            // convert to real data types here
            date = formatter.parse(sDate);
            close = Double.valueOf(sClose);
            high = Double.valueOf(sHigh);
            open = Double.valueOf(sOpen);
            low = Double.valueOf(sLow);
            volume = Integer.valueOf(sVolume);
            // validate
            if (date.getTime() > 0 && (close > 0.0 || high > 0.0 || open > 0.0 || low > 0.0)) {
               hd = new HistoricalData(date,open,high,low,close,volume,false);
            } else {
                LOGGER.log(Level.FINE,"failed validation of data on parse, skipping this line" + line.toString());
            }

        } catch(Exception e) {
            e.printStackTrace();
            LOGGER.log(Level.FINE,"skipping line, exception parsing: " + line.toString());
        }

        if (hd != null) {
            insertData(hd);
        }
    }


}
