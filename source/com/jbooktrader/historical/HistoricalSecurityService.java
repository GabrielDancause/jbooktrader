package com.jbooktrader.historical;

import com.ib.client.Contract;
import com.jbooktrader.platform.model.Dispatcher;
import com.jbooktrader.platform.model.JBookTraderException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by: marcus
 * Date: 1/10/13
 * Time: 4:22 AM
 */
public class HistoricalSecurityService {

    private final String HISTORICAL_DIR = "historical";

    private List<HistoricalSecurity> securitiesList;
    private IHistoricalSecurityFactory historicalSecurityFactory;

    public HistoricalSecurityService(IHistoricalSecurityFactory factory) {
        securitiesList = new ArrayList<HistoricalSecurity>();
        historicalSecurityFactory = factory;
    }

    public HistoricalSecurity getHistoricalSecurity(String symbol, String type, String exchange, HistoricalSecurity.BarSize barSize) {
        for (HistoricalSecurity s: securitiesList) {
            if (s.getSymbol().equals(symbol) && s.getType().equals(type) && s.getExchange().equals(exchange) && s.getBarSize().equals(barSize)) {
                return s;
            }
        }
        // see if we can create a new one for the consumer
        HistoricalSecurity newSec = historicalSecurityFactory.getInstance(symbol,type,exchange,barSize);
        if (newSec != null) {
            securitiesList.add(newSec);
        }
        return newSec;
    }





}
