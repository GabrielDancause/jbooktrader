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

    public HistoricalSecurityService() {
        securitiesList = new ArrayList<HistoricalSecurity>();
    }

    public HistoricalSecurity getHistoricalSecurity(String symbol, String type, String exchange, HistoricalSecurity.BarSize barSize) {
        for (HistoricalSecurity s: securitiesList) {
            Contract c = s.getContract();
            if (c.m_symbol.equals(symbol) && c.m_secType.equals(type) && c.m_exchange.equals(exchange) && s.getBarSize().equals(barSize)) {
                return s;
            }
        }
        return null;
    }

    public void addHistoricalSecurity(HistoricalSecurity sec) throws JBookTraderException {
        HistoricalSecurity existing = getHistoricalSecurity(sec.getContract().m_symbol, sec.getContract().m_secType,sec.getContract().m_exchange, sec.getBarSize());
        if (existing != null) {
            throw new JBookTraderException("Error, HistoricalSecurity already existsed, this should never happen");
        }
        if (sec.getBarSize() != HistoricalSecurity.BarSize.day_1) {
            throw new JBookTraderException("Error, we only support 1day data at this time");
        }
        securitiesList.add(sec);
    }



}
