package com.jbooktrader.historical;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created by: marcus
 * Date: 1/11/13
 * Time: 6:24 PM
 */
public class TestHistoricalDataConverter {

    @Test
    public void testConversion() throws IOException {
        HistoricalDataConverter hdc = new HistoricalDataConverter();

        File from = new File("./data/ES-sample.txt");
        File to = new File("./data/ES-GLOBEX-IND.txt"); // well, not really the real index

        hdc.convertBookToDailyOCHL(from,to);
    }
}
