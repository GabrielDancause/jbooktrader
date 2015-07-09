package com.jarbitrager.strategy;

import com.ib.client.*;
import com.jarbitrager.indicator.*;
import com.jarbitrager.platform.commission.*;
import com.jarbitrager.platform.indicator.*;
import com.jarbitrager.platform.instrument.*;
import com.jarbitrager.platform.model.*;
import com.jarbitrager.platform.optimizer.*;
import com.jarbitrager.platform.schedule.*;
import com.jarbitrager.platform.strategy.*;
import com.jarbitrager.platform.util.*;

/**
 *
 */
public class GBP_GBP extends Strategy {

    // Technical indicators
    private final Indicator emaDifferentialInd;

    // Strategy parameters names
    private static final String PERIOD = "Period";
    private static final String ENTRY = "Entry";
    private final int entry;

    public GBP_GBP(StrategyParams optimizationParams) throws JArbitragerException {
        super(optimizationParams);

        Contract contract1 = ContractFactory.makeCashContract("GBP", "USD");
        Commission commission1 = CommissionFactory.getForexCommission();
        Instrument instrument1 = new Instrument(contract1, commission1, 1);

        Contract contract2 = ContractFactory.makeFutureContract("GBP", "GLOBEX");
        Commission commission2 = CommissionFactory.getBundledNorthAmericaFutureCommission();
        Instrument instrument2 = new Instrument(contract2, commission2, 62500);

        TradingSchedule tradingSchedule = new TradingSchedule("9:35", "15:55", "America/New_York");
        setStrategy(instrument1, instrument2, tradingSchedule);

        entry = getParam(ENTRY);
        emaDifferentialInd = new EmaDifferential(getParam(PERIOD));
        addIndicator(emaDifferentialInd);
    }

    /**
     * Adds parameters to strategy. Each parameter must have 5 values: name:
     * identifier min, max, step: range for optimizer value: used in backtesting
     * and trading
     */
    @Override
    public void setParams() {
        addParam(PERIOD, 10, 1200, 10, 300);
        addParam(ENTRY, 1, 30, 1, 17);
    }

    /**
     * This method is invoked by the framework when an order book changes and
     * the technical indicators are recalculated. This is where the strategy
     * itself should be defined.
     */
    @Override
    public void evaluate() {
        double emaDifferential = emaDifferentialInd.getValue() * 100000;
        if (emaDifferential <= -entry) {
            instrument1.setTargetPosition(62500);
            instrument2.setTargetPosition(-1);
        } else if (emaDifferential >= entry) {
            instrument1.setTargetPosition(-62500);
            instrument2.setTargetPosition(1);
        }
    }
}
