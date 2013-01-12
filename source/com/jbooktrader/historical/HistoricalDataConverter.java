package com.jbooktrader.historical;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import com.jbooktrader.historical.impl.FileBackedHistoricalSecurity;
import com.jbooktrader.historical.model.HistoricalData;
import com.jbooktrader.platform.backtest.BackTestFileReader;
import com.jbooktrader.platform.startup.JBookTrader;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by: marcus
 * Date: 1/11/13
 * Time: 5:33 PM
 */
public class HistoricalDataConverter {

    private static final String LINE_SEP = System.getProperty("line.separator");

    private SimpleDateFormat formatter;
    private TimeZone timezone;


    private String currentDate;
    private double currentHigh;
    private double currentLow;
    private double currentOpen;
    private double currentClose;
    private int currentVolume;

    private static final Logger LOGGER = Logger.getLogger(HistoricalDataConverter.class.getName());
    private boolean headerWritten;

    public HistoricalDataConverter() {
        formatter = new SimpleDateFormat("MMddyy:HHmmss");
        headerWritten = false;
    }

    /**
     *
     * Convert jbooktrader book data to high/low files for historical use
     * Note, since this isn't getting its src from an index, you do get errors with contract crossover, this is used for testing for now
     *
     */
    public int convertBookToDailyOCHL(File from, File to) throws IOException {
        int rows = 0;
        FileReader reader = new FileReader(from);
        FileWriter writer = new FileWriter(to);
        CSVReader csvReader = new CSVReader(reader);
        CSVWriter csvWriter = new CSVWriter(writer,CSVWriter.DEFAULT_SEPARATOR,CSVWriter.NO_QUOTE_CHARACTER);


        currentDate = null;
        currentClose = currentHigh = currentOpen = currentLow = currentVolume = 0;

        String[] nextline;
        while ((nextline = csvReader.readNext()) != null) {
            if (nextline[0].startsWith("timeZone=")) {
                parseTimeZone(nextline[FileBackedHistoricalSecurity.DATE]);
            }
            if (!(nextline[FileBackedHistoricalSecurity.DATE].startsWith("#") || nextline[FileBackedHistoricalSecurity.DATE].length() == 0 || nextline.length < 5)) {
                if (!headerWritten) {
                    printFileNotes(writer);
                }
                processLine(nextline, csvWriter);
            }
        }

        csvWriter.close();
        csvReader.close();
        return rows;
    }

    private void printFileNotes(FileWriter writer) throws IOException {
        StringBuilder header = new StringBuilder();
        header.append("# This historical data file was created by ").append(JBookTrader.APP_NAME).append(LINE_SEP);
        header.append("# Each line represents a 1-day snapshot of the market and contains ").append(BackTestFileReader.COLUMNS).append(" columns:").append(LINE_SEP);
        header.append("# 1. date in the MMddyy format").append(LINE_SEP);
        header.append("# 2. Open").append(LINE_SEP);
        header.append("# 3. Close").append(LINE_SEP);
        header.append("# 4. High").append(LINE_SEP);
        header.append("# 5  Low").append(LINE_SEP);
        header.append("# 6. Volume").append(LINE_SEP);
        header.append(LINE_SEP);
        header.append("timeZone=").append(formatter.getTimeZone().getID()).append(LINE_SEP);
        writer.write(header.toString());
        headerWritten = true;
    }

    private void parseTimeZone(String tz) {
        int idx = tz.indexOf('=');
        String ourTZ = tz.substring(idx+1);
        this.timezone = TimeZone.getTimeZone(ourTZ);
        formatter.setTimeZone(timezone);
    }

    private void processLine(String[] line, CSVWriter csvWriter) {

        String sDate = line[0];
        String sTime = line[1];
        String sPrice = line[3];
        String sVolume = line[4];



        Date date;
        double open;  // using double here, can lead to iteration rounding errors, it is smaller than BigDecimal, and follows existing project datatypes
        double price;

        int volume;

        String[] row = null;



        try {
            // convert to real data types here
            date = formatter.parse(sDate+":"+sTime);
            price = Double.valueOf(sPrice);
            volume = Integer.valueOf(sVolume);

            // validate
            if (date.getTime() > 0 && (price > 0.0 )) {
                if (!sDate.equals(currentDate)) {
                    if (currentDate != null) {
                        // write out row
                        row = new String[6];
                        row[FileBackedHistoricalSecurity.DATE] = currentDate;
                        row[FileBackedHistoricalSecurity.OPEN] = Double.toString(currentOpen);
                        row[FileBackedHistoricalSecurity.CLOSE] = Double.toString(currentClose);
                        row[FileBackedHistoricalSecurity.LOW] = Double.toString(currentLow);
                        row[FileBackedHistoricalSecurity.HIGH] = Double.toString(currentLow);
                        row[FileBackedHistoricalSecurity.VOLUME] = Integer.toString(currentVolume);

                        csvWriter.writeNext(row);
                        csvWriter.flush();
                    }
                    currentDate = sDate; // now make it current, and reset data
                    currentOpen = price;
                    currentLow = price;
                    currentClose = currentHigh = currentVolume = 0;

                }
                currentHigh = (price > currentHigh) ? price: currentHigh;
                currentLow = (price < currentLow) ? price: currentLow;


                currentClose = price; // in case this was our last row
                currentVolume += volume;


            } else {
                LOGGER.log(Level.FINE,"failed validation of data on parse, skipping this line" + line.toString());
            }

        } catch(Exception e) {
            e.printStackTrace();
            LOGGER.log(Level.FINE,"skipping line, exception parsing: " + line.toString());
        }


    }
}
