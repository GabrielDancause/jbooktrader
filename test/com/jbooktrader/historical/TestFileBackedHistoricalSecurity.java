package com.jbooktrader.historical;

import com.jbooktrader.historical.impl.FileBackedHistoricalSecurity;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by: marcus
 * Date: 1/11/13
 * Time: 1:30 AM
 */
public class TestFileBackedHistoricalSecurity {

    @Test
    public void TestFileBackedConstructor() throws IOException {
        File f = new File("test/historical.test");
        if (!f.exists()) {
            assertTrue(false);
        }
        FileBackedHistoricalSecurity fbs = new FileBackedHistoricalSecurity(f.getPath(),"ES","IND","GLOBEX",HistoricalSecurity.BarSize.day_1);
        assertEquals(2,fbs.size());
        String tz = fbs.getTimezone().getID();
        assertEquals("PST",tz);
    }

}
