package com.jarbitrager.platform.backtest;

import com.jarbitrager.platform.instrument.*;
import com.jarbitrager.platform.model.*;

import java.io.*;
import java.text.*;
import java.util.*;


/**
 * Reads and validates a data file containing historical market snapshot records.
 * The data file is used for backtesting and optimization of trading strategies.
 */
public class BackTestFileReader {
    public final static int COLUMNS = 6;
    private static final String LINE_SEP = System.getProperty("line.separator");
    private final String fileName;
    private long previousTime;
    private SimpleDateFormat sdf;
    private boolean instrumentsDefined;
    private volatile boolean cancelled;
    private BufferedReader reader;
    private long snapshotCount, firstMarketLine, lineNumber;


    public BackTestFileReader(String fileName) throws JArbitragerException {
        this.fileName = fileName;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
        } catch (FileNotFoundException fnfe) {
            throw new JArbitragerException("Could not find file " + fileName);
        }
    }

    public void cancel() {
        cancelled = true;
    }

    public long getSnapshotCount() {
        return snapshotCount;
    }

    private void setTimeZone(String line) throws JArbitragerException {
        String timeZone = line.substring(line.indexOf('=') + 1);
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        if (!tz.getID().equals(timeZone)) {
            String msg = "The specified time zone " + "\"" + timeZone + "\"" + " does not exist." + LINE_SEP;
            msg += "Examples of valid time zones: " + " America/New_York, Europe/London, Asia/Singapore.";
            throw new JArbitragerException(msg);
        }
        sdf = new SimpleDateFormat("MMddyy,HHmmss");
        // Enforce strict interpretation of date and time formats
        sdf.setLenient(false);
        sdf.setTimeZone(tz);
    }


    public void scan(String instruments) throws JArbitragerException {
        String line;

        try {
            while ((line = reader.readLine()) != null && !cancelled) {
                lineNumber++;
                boolean isComment = line.startsWith("#");
                boolean isProperty = line.contains("=");
                boolean isBlankLine = (line.trim().length() == 0);
                boolean isMarketSnapshotLine = !(isComment || isProperty || isBlankLine);
                if (isMarketSnapshotLine) {
                    snapshotCount++;
                    if (firstMarketLine == 0) {
                        firstMarketLine = lineNumber;
                    }
                } else if (isProperty) {
                    if (line.startsWith("timeZone")) {
                        setTimeZone(line);
                    }

                    if (line.startsWith("instruments")) {
                        String fileInstruments = line.substring(line.indexOf('=') + 1);
                        if (!instruments.equals(fileInstruments)) {
                            String error = "The strategy defines instruments " + instruments + ". ";
                            error += "The selected data file contains instruments " + fileInstruments + ". Please select another data file.";
                            throw new JArbitragerException(error);
                        }
                        instrumentsDefined = true;
                    }
                }
            }

            if (sdf == null) {
                String msg = "Property " + "\"timeZone\"" + " is not defined in the data file." + LINE_SEP;
                throw new JArbitragerException(msg);
            }

            if (!instrumentsDefined) {
                String msg = "Property " + "\"instruments\"" + " is not defined in the data file." + LINE_SEP;
                throw new JArbitragerException(msg);
            }

            reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
            for (int lineCount = 1; lineCount < firstMarketLine; lineCount++) {
                reader.readLine();
            }
            lineNumber = firstMarketLine;

        } catch (IOException ioe) {
            throw new JArbitragerException("Could not read data file");
        }

    }

    public MarketSnapshot next() {
        String line = "";
        MarketSnapshot marketSnapshot = null;

        try {
            while (marketSnapshot == null) {
                line = reader.readLine();

                if (line == null) {
                    reader.close();
                    break;
                } else {
                    marketSnapshot = toMarketSnapshot(line);
                    lineNumber++;
                    previousTime = marketSnapshot.getTime();
                }
            }
        } catch (IOException ioe) {
            throw new RuntimeException("Could not read data file");
        } catch (JArbitragerException e) {
            String errorMsg = "";
            if (lineNumber > 0) {
                errorMsg = "Problem parsing line #" + lineNumber + LINE_SEP;
                errorMsg += line + LINE_SEP;
            }
            String description = e.getMessage();
            if (description == null) {
                description = e.toString();
            }
            errorMsg += description;
            throw new RuntimeException(errorMsg);
        }

        return marketSnapshot;
    }

    private MarketSnapshot toMarketSnapshot(String line) throws JArbitragerException {
        StringTokenizer st = new StringTokenizer(line, ",");

        int tokenCount = st.countTokens();
        if (tokenCount != COLUMNS) {
            String msg = "The line should contain exactly " + COLUMNS + " comma-separated columns.";
            throw new JArbitragerException(msg);
        }

        String dateToken = st.nextToken();
        String timeToken = st.nextToken();
        long time;
        try {
            time = sdf.parse(dateToken + "," + timeToken).getTime();
        } catch (ParseException pe) {
            throw new JArbitragerException("Could not parse date/time in " + dateToken + "," + timeToken);
        }

        if (previousTime != 0) {
            if (time < previousTime) {
                String msg = "Timestamp of this line is before or the same as the timestamp of the previous line.";
                throw new JArbitragerException(msg);
            }
        }

        double bid1 = Double.parseDouble(st.nextToken());
        double ask1 = Double.parseDouble(st.nextToken());
        double bid2 = Double.parseDouble(st.nextToken());
        double ask2 = Double.parseDouble(st.nextToken());

        return new MarketSnapshot(time, bid1, ask1, bid2, ask2);

    }
}
