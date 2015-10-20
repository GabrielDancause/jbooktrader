package com.jarbitrager.platform.position;

import com.ib.client.*;
import com.jarbitrager.platform.chart.*;
import com.jarbitrager.platform.instrument.*;
import com.jarbitrager.platform.model.*;
import com.jarbitrager.platform.performance.*;
import com.jarbitrager.platform.report.*;
import com.jarbitrager.platform.strategy.*;
import com.jarbitrager.platform.trader.*;
import com.jarbitrager.platform.util.*;


/**
 * Position manager keeps track of current positions and executions.
 */
public class PositionManager {
    private final Dispatcher dispatcher;
    private final Strategy strategy;
    private final EventReport eventReport;
    private final TraderAssistant traderAssistant;
    private final PerformanceManager performanceManager;
    private volatile boolean orderExecutionPending;

    public PositionManager(Strategy strategy) {
        this.strategy = strategy;
        dispatcher = Dispatcher.getInstance();
        eventReport = dispatcher.getEventReport();
        traderAssistant = dispatcher.getTrader().getAssistant();
        performanceManager = strategy.getPerformanceManager();
    }

    public synchronized void update(OpenOrder openOrder) {
        Order order = openOrder.getOrder();
        String action = order.m_action;
        int sharesFilled = openOrder.getSharesFilled();
        int quantity = 0;

        if (action.equals("SELL")) {
            quantity = -sharesFilled;
        }

        if (action.equals("BUY")) {
            quantity = sharesFilled;
        }

        // current position after the execution
        Instrument instrument = openOrder.getInstrument();
        instrument.setCurrentPosition(instrument.getCurrentPosition() + quantity);
        double avgFillPrice = openOrder.getAvgFillPrice();
        instrument.setAveFillPrice(avgFillPrice);

        performanceManager.updateOnTrade(quantity, avgFillPrice, instrument);
        if (dispatcher.getMode() == Mode.BackTest) {
            PerformanceChartData pcd = strategy.getPerformanceChartData();
            pcd.updateNetProfit(new TimedValue(strategy.getTime(), performanceManager.getNetProfit()));
            pcd.updateAnnotations(new Position(openOrder.getDate(), instrument.getCurrentPosition(), avgFillPrice));
        }

        Mode mode = dispatcher.getMode();

        if (mode == Mode.ForwardTest || mode == Mode.Trade) {
            StringBuilder msg = new StringBuilder();
            msg.append("Instrument: ").append(instrument).append(": ");
            msg.append("Order ").append(openOrder.getId()).append(" is filled.  ");
            msg.append("Average fill price: ").append(avgFillPrice).append(". ");
            msg.append("Position: ").append(instrument.getCurrentPosition());
            eventReport.report(strategy.getName(), msg.toString());
        }


        if (strategy.hasCompletedTrade()) {
            orderExecutionPending = false;
            if (mode != Mode.Optimization) {
                strategy.getStrategyReportManager().report();
            }
        }
    }

    public void resetOrderExecutionPending() {
        orderExecutionPending = false;
    }

    public void trade() {
        Instrument instrument1 = strategy.getInstrument1();
        Instrument instrument2 = strategy.getInstrument2();

        if (!orderExecutionPending) {
            int quantity1 = instrument1.getTargetPosition() - instrument1.getCurrentPosition();
            int quantity2 = instrument2.getTargetPosition() - instrument2.getCurrentPosition();
            if (quantity1 != 0 && quantity2 != 0) {
                if (instrument1.hasNormalBidAskSpread() && instrument2.hasNormalBidAskSpread()) {
                    String action1 = (quantity1 > 0) ? "BUY" : "SELL";
                    String action2 = (quantity2 > 0) ? "BUY" : "SELL";
                    orderExecutionPending = true;
                    traderAssistant.placeMarketOrder(instrument1, Math.abs(quantity1), action1, strategy);
                    traderAssistant.placeMarketOrder(instrument2, Math.abs(quantity2), action2, strategy);
                }
            }
        }

    }
}
