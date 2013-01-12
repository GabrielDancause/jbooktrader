package com.jbooktrader.historical;

import com.jbooktrader.historical.model.HistoricalData;
import org.junit.Test;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.SortedMap;

import static junit.framework.Assert.*;

/**
 * Created by: marcus
 * Date: 1/10/13
 * Time: 1:29 AM
 */
public class TestHistoricalSecurity {



    @Test
    public void testInsertData() {

        HistoricalSecurity hsb = new HistoricalSecurity("ES","IND","GLOBEX",HistoricalSecurity.BarSize.day_1);

        Date d1 = new Date();

        HistoricalData data = new HistoricalData(d1,0,0,0,0,0,false);

        hsb.insertData(data);
        HistoricalData hd = hsb.getData(d1);

        assertEquals(hd,data);
    }

    @Test
    public void testIsValidAndRange() {
        Date d1 = new Date();
        Date d2 = new Date(d1.getTime()+1000);
        Date d3 = new Date(d2.getTime()+1000);
        Date d4 = new Date(d3.getTime()+1000);
        Date d5 = new Date();
        d5.setTime(d4.getTime()+1000*60);

        HistoricalSecurity hsb = new HistoricalSecurity("ES","IND","GLOBEX",HistoricalSecurity.BarSize.day_1);
        HistoricalData data1 = new HistoricalData(d1,1,0,0,0,0,false);
        HistoricalData data2 = new HistoricalData(d2,2,0,0,0,0,false);
        HistoricalData data3 = new HistoricalData(d3,3,0,0,0,0,false);
        HistoricalData data4 = new HistoricalData(d4,4,0,0,0,0,false);

        hsb.insertData(data1);
        hsb.insertData(data2);
        hsb.insertData(data3);
        hsb.insertData(data4);

        assertTrue(hsb.size()==4);

        assertTrue(hsb.isValid(d2,d3));
        assertTrue(!hsb.isValid(d2,d5));
        assertTrue(hsb.isValid(d1,d4));
        assertTrue(hsb.isValid(d2,d2));

        SortedMap<Date,HistoricalData> map = hsb.getRange(d2, true, d4, true);
        assertTrue(map.size()==3);
        assertTrue(map.get(d2).equals(data2));
        SortedMap<Date,HistoricalData> map2 = hsb.getRange(d2, false, d4, false);
        assertTrue(map2.size()==1);

    }

}
