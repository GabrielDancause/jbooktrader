package com.jbooktrader.strategy;

import com.jbooktrader.historical.indicator.HistoricalEMA;
import com.jbooktrader.indicator.balance.BalanceVelocity;
import com.jbooktrader.indicator.price.PriceVelocity;
import com.jbooktrader.platform.indicator.Indicator;
import com.jbooktrader.platform.model.JBookTraderException;
import com.jbooktrader.platform.optimizer.StrategyParams;
import com.jbooktrader.strategy.base.StrategyES;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by: marcus
 * Date: 1/11/13
 * Time: 5:17 PM
 */
public class SampleHistorical extends StrategyES {
    private static Logger LOGGER = Logger.getLogger(SampleHistorical.class.getName());

    // Technical indicators
    private Indicator balanceVelocityInd, priceVelocityInd;
    private HistoricalEMA historicalEMA;



    // Strategy parameters names
    private static final String PERIOD = "Period";
    private static final String SCALE = "Scale";
    private static final String ENTRY = "Entry";
    private static final String EXIT = "Exit";
    private static final String HIST_EMA = "Hist EMA";

    // Strategy parameters values
    private final int entry, exit, scale;


    public SampleHistorical(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);

        entry = getParam(ENTRY);
        exit = getParam(EXIT);
        scale = getParam(SCALE);
    }

    @Override
    public void setParams() {
        addParam(PERIOD, 2200, 3600, 5, 3200);
        addParam(SCALE, 5, 25, 1, 16);
        addParam(ENTRY, 55, 120, 1, 92);
        addParam(EXIT, -50, 0, 1, -21);
        addParam(HIST_EMA, 4, 100, 4, 8);
    }

    @Override
    public void setIndicators() {
        balanceVelocityInd = addIndicator(new BalanceVelocity(1, getParam(PERIOD)));
        priceVelocityInd = addIndicator(new PriceVelocity(1, getParam(PERIOD)));
        historicalEMA = (HistoricalEMA)addIndicator(new HistoricalEMA(10,"ES","IND","GLOBEX")); // watch this historical indicator also

    }

    @Override
    public void onBookSnapshot() {
        double balanceVelocity = balanceVelocityInd.getValue();
        double priceVelocity = priceVelocityInd.getValue();

        if (!historicalEMA.isValid()) {
            //LOGGER.log(Level.FINE,"skipping, no valid historical EMA at this point " + getMarketBook().getSnapshot().toString());
            setPosition(0); // just in case, close out position
            return;
        }

        double force = balanceVelocity - scale * priceVelocity;
        if (force >= entry && balanceVelocity > 0 && priceVelocity < 0) {
            setPosition(1);
        } else if (force <= -exit) {
            setPosition(0);
        }
    }
}
