package com.jbooktrader.historical;

import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * Created by: marcus
 * Date: 1/10/13
 * Time: 1:03 PM
 */
public class FileBackedHistoricalSecurity extends HistoricalSecurity {
    protected String filePath;

    public FileBackedHistoricalSecurity(String filePath) throws FileNotFoundException {
        this.filePath = filePath;
        loadFromFile(filePath);
    }

    private void loadFromFile(String filePath) throws FileNotFoundException {
        FileReader fr = new FileReader(filePath);
    }

}
