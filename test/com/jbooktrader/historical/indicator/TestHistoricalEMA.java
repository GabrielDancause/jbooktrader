package com.jbooktrader.historical.indicator;

import com.jbooktrader.historical.model.HistoricalData;
import com.jbooktrader.platform.marketbook.MarketBook;
import com.jbooktrader.platform.marketbook.MarketSnapshot;
import com.jbooktrader.platform.model.JBookTraderException;
import com.jbooktrader.platform.startup.JBookTrader;
import org.junit.Test;
import sun.jvm.hotspot.debugger.win32.coff.TestDebugInfo;

import java.util.Calendar;
import java.util.TimeZone;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by: marcus
 * Date: 1/11/13
 * Time: 2:52 PM
 */
public class TestHistoricalEMA {

    @Test
    public void testHistoricalEMAConstruction() throws JBookTraderException {
        JBookTrader.setAppPath("./test");
        JBookTrader app = new JBookTrader();
        HistoricalEMA he = new HistoricalEMA(3, "ES", "IND", "GLOBEX");

        assertTrue(!he.isValid());

        // prepare indicator for use
        he.setMarketBook(createTestMarketBook());
        he.calculate();
        assertTrue(he.isValid());

        double value = he.getValue();
        assertEquals(3.0625,value);
    }

    public void testHistoricalEMAShortOnSamplesForPeriod() throws JBookTraderException {
        JBookTrader.setAppPath("./test");
        JBookTrader app = new JBookTrader();
        HistoricalEMA he = new HistoricalEMA(5, "ES", "IND", "GLOBEX");

        assertTrue(!he.isValid());

        // prepare indicator for use
        MarketBook mb = createTestMarketBook();
        he.setMarketBook(mb);
        he.calculate();
        assertTrue(!he.isValid());

        Calendar c = Calendar.getInstance(mb.getTimeZone());
        c.set(2013,Calendar.JANUARY,5,0,0,0);

        HistoricalData hd = new HistoricalData(c.getTime(),1,0,0,0,0,false);
        he.getHistoricalSecurity().insertData(hd); // we added data for the 5th, our test day.  This still doesn't give us 5 prior days, so we are still expected to be invalid

        he.calculate();
        assertTrue(!he.isValid());

        c.set(2012,Calendar.DECEMBER,1,0,0,0); // some older date, which actually represents a gap in prior data
        HistoricalData hd2 = new HistoricalData(c.getTime(),10,0,0,0,0,false);
        he.getHistoricalSecurity().insertData(hd2);
        // we should be valid now, however we do have a gap.  We don't check for gaps yet, so it is used
        he.calculate();
        assertTrue(he.isValid());


    }




    public MarketBook createTestMarketBook() {
        MarketBook mb = new MarketBook("ES-GLOBEX-IND", TimeZone.getTimeZone("EST"));
        Calendar calendar = Calendar.getInstance(mb.getTimeZone());
        calendar.set(2013,Calendar.JANUARY,5,0,0,0);
        MarketSnapshot snap = new MarketSnapshot(calendar.getTimeInMillis(),0,0,0);
        mb.setSnapshot(snap); // snapshot only needs correct date for this test
        return mb;
    }
}
