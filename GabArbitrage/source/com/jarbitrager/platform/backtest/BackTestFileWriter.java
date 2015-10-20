package com.jarbitrager.platform.backtest;

import com.jarbitrager.platform.instrument.*;
import com.jarbitrager.platform.model.*;
import com.jarbitrager.platform.startup.*;
import com.jarbitrager.platform.strategy.*;
import com.jarbitrager.platform.util.*;

import java.io.*;
import java.text.*;
import java.util.*;

/**
 * Writes historical market data to a file which is used for backtesting and
 * optimization of trading strategies.
 */
public class BackTestFileWriter {
    private static final String FILE_SEP = System.getProperty("file.separator");
    private static final String LINE_SEP = System.getProperty("line.separator");
    private static final String MARKET_DATA_DIR = JArbitrager.getAppPath() + FILE_SEP + "marketData";
    private final DecimalFormat decimalFormat;
    private final SimpleDateFormat dateFormat;
    private PrintWriter writer;

    public BackTestFileWriter(Strategy strategy) throws JArbitragerException {
        decimalFormat = NumberFormatterFactory.getNumberFormatter(5);
        dateFormat = new SimpleDateFormat("MMddyy,HHmmss");
        dateFormat.setTimeZone(strategy.getTradingSchedule().getTimeZone());

        File marketDataDir = new File(MARKET_DATA_DIR);
        if (!marketDataDir.exists()) {
            marketDataDir.mkdir();
        }

        String fullFileName = MARKET_DATA_DIR + FILE_SEP + strategy.getName() + ".txt";
        try {
            boolean fileExisted = new File(fullFileName).exists();
            writer = new PrintWriter(new BufferedWriter(new FileWriter(fullFileName, true)));
            if (!fileExisted) {
                StringBuilder header = getHeader(strategy.getInstrument1().getContract().m_symbol, strategy.getInstrument2().getContract().m_symbol);
                writer.println(header);
            }
        } catch (IOException ioe) {
            throw new JArbitragerException("Could not write to file " + strategy.getName());
        }
    }

    public void write(long time, Instrument instrument1, Instrument instrument2) {
        double bid1 = instrument1.getBid();
        double ask1 = instrument1.getAsk();
        double bid2 = instrument2.getBid();
        double ask2 = instrument2.getAsk();

        if (bid1 > 0 && ask1 > 0 && bid2 > 0 && ask2 > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(dateFormat.format(new Date(time))).append(",");
            sb.append(decimalFormat.format(bid1)).append(",");
            sb.append(decimalFormat.format(ask1)).append(",");
            sb.append(decimalFormat.format(bid2)).append(",");
            sb.append(decimalFormat.format(ask2));

            writer.println(sb);
            writer.flush();
        }
    }

    public void close() {
        writer.close();
    }

    private StringBuilder getHeader(String symbol1, String symbol2) {
        StringBuilder header = new StringBuilder();
        String appInfo = JArbitrager.APP_NAME + ", version " + JArbitrager.VERSION;
        header.append("# This historical data file was created by ").append(appInfo).append(LINE_SEP);
        header.append("# Each line represents a 1-second snapshot of the market and contains ").append(BackTestFileReader.COLUMNS).append(" columns:").append(
                LINE_SEP);
        header.append("# 1. date in the MMddyy format").append(LINE_SEP);
        header.append("# 2. time in the HHmmss format").append(LINE_SEP);
        header.append("# 3. best bid for the first instrument").append(LINE_SEP);
        header.append("# 4. best ask for the first instrument").append(LINE_SEP);
        header.append("# 5. best bid for the second instrument").append(LINE_SEP);
        header.append("# 6. best ask for the second instrument").append(LINE_SEP);
        header.append(LINE_SEP);
        header.append("instruments=").append(symbol1).append(",").append(symbol2).append(LINE_SEP);
        header.append("timeZone=").append(dateFormat.getTimeZone().getID()).append(LINE_SEP);
        return header;
    }
}
