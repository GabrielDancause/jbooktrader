package com.jarbitrager.platform.strategy;

import com.jarbitrager.platform.backtest.*;
import com.jarbitrager.platform.chart.*;
import com.jarbitrager.platform.indicator.*;
import com.jarbitrager.platform.instrument.*;
import com.jarbitrager.platform.model.*;
import com.jarbitrager.platform.optimizer.*;
import com.jarbitrager.platform.performance.*;
import com.jarbitrager.platform.position.*;
import com.jarbitrager.platform.report.*;
import com.jarbitrager.platform.schedule.*;


/**
 * Base class for all classes that implement trading strategies.
 */

public abstract class Strategy implements Comparable<Strategy> {
    private final StrategyParams params;
    private final EventReport eventReport;
    private final String name;
    private Dispatcher dispatcher;
    private TradingSchedule tradingSchedule;
    private PositionManager positionManager;
    private PerformanceManager performanceManager;
    private StrategyReportManager strategyReportManager;
    private IndicatorManager indicatorManager;
    private PerformanceChartData performanceChartData;
    private boolean isActive;
    private long time;
    private BackTestFileWriter backTestFileWriter;
    protected Instrument instrument1, instrument2;

    /**
     * Framework calls this method to evaluate the strategy.
     */
    abstract public void evaluate();

    /**
     * Framework calls this method to set strategy parameter ranges and values.
     */
    abstract protected void setParams();

    protected Strategy(StrategyParams params) {
        this.params = params;
        if (params.size() == 0) {
            setParams();
        }

        name = getClass().getSimpleName();
        dispatcher = Dispatcher.getInstance();
        eventReport = dispatcher.getEventReport();
    }

    public boolean isActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public void setSnapshot(MarketSnapshot marketSnapshot) {
        setTime(marketSnapshot.getTime());
        instrument1.setBid(marketSnapshot.getBid1());
        instrument1.setAsk(marketSnapshot.getAsk1());
        instrument2.setBid(marketSnapshot.getBid2());
        instrument2.setAsk(marketSnapshot.getAsk2());
    }

    public void closePosition() {
        instrument1.setTargetPosition(0);
        instrument2.setTargetPosition(0);

        if (!hasCompletedTrade()) {
            Mode mode = dispatcher.getMode();
            if (mode == Mode.ForwardTest || mode == Mode.Trade) {
                String msg = "End of trading interval. Closing current position.";
                eventReport.report(getName(), msg);
            }
        }
    }

    public StrategyParams getParams() {
        return params;
    }

    protected int getParam(String name) throws JArbitragerException {
        return params.get(name).getValue();
    }

    protected void addParam(String name, int min, int max, int step, int value) {
        params.add(name, min, max, step, value);
    }

    public PositionManager getPositionManager() {
        return positionManager;
    }

    public PerformanceManager getPerformanceManager() {
        return performanceManager;
    }

    public StrategyReportManager getStrategyReportManager() {
        return strategyReportManager;
    }

    public IndicatorManager getIndicatorManager() {
        return indicatorManager;
    }

    public TradingSchedule getTradingSchedule() {
        return tradingSchedule;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    protected void addIndicator(Indicator indicator) {
        indicatorManager.addIndicator(indicator);
        performanceChartData.addIndicator(indicator);
    }

    protected void setStrategy(Instrument instrument1, Instrument instrument2, TradingSchedule tradingSchedule) {
        this.instrument1 = instrument1;
        this.instrument2 = instrument2;
        this.tradingSchedule = tradingSchedule;
        performanceChartData = new PerformanceChartData(instrument1.getContract().m_symbol, instrument2.getContract().m_symbol);
        performanceManager = new PerformanceManager(this);
        positionManager = new PositionManager(this);
        strategyReportManager = new StrategyReportManager(this);
        indicatorManager = new IndicatorManager();
    }

    public String getInstruments() {
        return instrument1.getContract().m_symbol + "," + instrument2.getContract().m_symbol;
    }

    public PerformanceChartData getPerformanceChartData() {
        return performanceChartData;
    }

    public Instrument getInstrument1() {
        return instrument1;
    }

    public Instrument getInstrument2() {
        return instrument2;
    }

    public Instrument getInstrumentForTicker(int tickerId) {
        if (instrument1.getTickerId() == tickerId) {
            return instrument1;
        }
        if (instrument2.getTickerId() == tickerId) {
            return instrument2;
        }

        return null;
    }


    public String getName() {
        return name;
    }

    public boolean hasValidInstruments() {
        return (instrument1.isValid() && instrument2.isValid());
    }

    public void process() {
        boolean isInSchedule = getTradingSchedule().contains(time);
        process(isInSchedule);
    }

    public void process(boolean isInSchedule) {
        indicatorManager.updateIndicators(time);
        if (isInSchedule) {
            if (indicatorManager.hasValidIndicators()) {
                evaluate();
            }
        } else {
            closePosition();// force flat position
        }

        positionManager.trade();
        performanceManager.updatePositionValue();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" ").append(name);
        sb.append(" [");
        sb.append(instrument1).append("-");
        sb.append(instrument2).append("-");
        sb.append("]");

        return sb.toString();
    }

    public String indicatorsState() {
        String indicatorsState = "";
        for (Indicator indicator : indicatorManager.getIndicators()) {
            if (!indicatorsState.isEmpty()) {
                indicatorsState += ",";
            }

            indicatorsState += (int) indicator.getValue();
        }
        return indicatorsState;
    }

    public boolean hasCompletedTrade() {
        return (instrument1.hasCompletedTrade() && instrument2.hasCompletedTrade());
    }

    public int compareTo(Strategy other) {
        return name.compareTo(other.name);
    }

    public void saveSnapshot() {
        if (backTestFileWriter == null) {
            try {
                backTestFileWriter = new BackTestFileWriter(this);
            } catch (JArbitragerException e) {
                throw new RuntimeException(e);
            }
        }
        backTestFileWriter.write(getTime(), instrument1, instrument2);
    }

}
