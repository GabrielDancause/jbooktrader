package com.jarbitrager.platform.trader;

import com.ib.client.*;
import com.jarbitrager.platform.instrument.*;
import com.jarbitrager.platform.model.*;
import com.jarbitrager.platform.position.*;
import com.jarbitrager.platform.report.*;
import com.jarbitrager.platform.startup.*;
import com.jarbitrager.platform.strategy.*;

import java.util.*;

/**
 * This class acts as a "wrapper" in the IB's API terminology.
 */
public class Trader extends EWrapperAdapter {
    private final EventReport eventReport;
    private final TraderAssistant traderAssistant;
    private String previousErrorMessage;

    public Trader() {
        traderAssistant = new TraderAssistant(this);
        previousErrorMessage = "";
        eventReport = Dispatcher.getInstance().getEventReport();
    }

    public TraderAssistant getAssistant() {
        return traderAssistant;
    }

    @Override
    public void updateAccountValue(String key, String value, String currency, String accountName) {
        try {
            if (key.equalsIgnoreCase("AccountCode")) {
                synchronized (this) {
                    traderAssistant.setAccountCode(value);
                    notifyAll();
                }
            }
        } catch (Throwable t) {
            // Do not allow exceptions come back to the socket -- it will cause disconnects
            eventReport.report(t);
        }
    }

    @Override
    public void updateNewsBulletin(int msgId, int msgType, String message, String origExchange) {
        String newsBulletin = "Msg ID: " + msgId + " Msg Type: " + msgType + " Msg: " + message + " Exchange: " + origExchange;
        eventReport.report("IB", newsBulletin);
    }

    @Override
    public void execDetails(int reqId, Contract contract, Execution execution) {
        try {
            int orderId = execution.m_orderId;
            Map<Integer, OpenOrder> openOrders = traderAssistant.getOpenOrders();
            OpenOrder openOrder = openOrders.get(orderId);
            if (openOrder != null) {
                openOrder.add(execution);
                if (openOrder.isFilled()) {
                    Strategy strategy = openOrder.getStrategy();
                    PositionManager positionManager = strategy.getPositionManager();
                    positionManager.update(openOrder);
                    openOrders.remove(orderId);
                }
            }
        } catch (Throwable t) {
            // Do not allow exceptions come back to the socket -- it will cause disconnects
            eventReport.report(t);
        }
    }

    @Override
    public void execDetailsEnd(int reqId) {
        try {
            Map<Integer, OpenOrder> openOrders = traderAssistant.getOpenOrders();

            for (OpenOrder openOrder : openOrders.values()) {
                String msg = "Execution for order " + openOrder.getId() + " was not found.";
                msg += " In all likelihood, this is because the order was placed while TWS was disconnected from the server.";
                msg += " This order will be removed and another one will be submitted. The strategy will continue to run normally.";
                eventReport.report(JArbitrager.APP_NAME, msg);
                Strategy strategy = openOrder.getStrategy();
                PositionManager positionManager = strategy.getPositionManager();
                positionManager.resetOrderExecutionPending();
            }

            openOrders.clear();
        } catch (Throwable t) {
            // Do not allow exceptions come back to the socket -- it will cause disconnects
            eventReport.report(t);
        }
    }

    @Override
    public void contractDetails(int id, ContractDetails contractDetails) {
        String lineSep = "<br>";
        StringBuilder details = new StringBuilder("Contract details:").append(lineSep);
        details.append("trading class: ").append(contractDetails.m_tradingClass).append(lineSep);
        details.append("valid exchanges: ").append(contractDetails.m_validExchanges).append(lineSep);
        details.append("long name: ").append(contractDetails.m_longName).append(lineSep);
        details.append("market name: ").append(contractDetails.m_marketName).append(lineSep);
        details.append("min tick: ").append(contractDetails.m_minTick).append(lineSep);
        details.append("contractMonth: ").append(contractDetails.m_contractMonth).append(lineSep);
        details.append("industry: ").append(contractDetails.m_industry).append(lineSep);
        details.append("category: ").append(contractDetails.m_category).append(lineSep);
        details.append("subcategory: ").append(contractDetails.m_subcategory).append(lineSep);
        details.append("timeZoneId: ").append(contractDetails.m_timeZoneId).append(lineSep);
        details.append("tradingHours: ").append(contractDetails.m_tradingHours).append(lineSep);
        details.append("liquidHours: ").append(contractDetails.m_liquidHours).append(lineSep);
        eventReport.report("IB", details.toString());
    }

    @Override
    public void error(Exception e) {
        eventReport.report("IB", e.toString());
    }

    @Override
    public void error(String error) {
        eventReport.report("IB", error);
    }

    @Override
    public void error(int id, int errorCode, String errorMsg) {
        try {
            String msg = errorCode + ": " + errorMsg;
            if (id != -1) {
                msg += " (for id " + id + ")";
            }

            if (msg.equals(previousErrorMessage)) {
                // ignore duplicate error messages
                return;
            }

            previousErrorMessage = msg;
            eventReport.report("IB", msg);

            // Errors 1101 and 1102 are sent when connectivity is restored. However, sometimes TWS fails
            // to send these error codes. To compensate, we also listen for error code 2104 which indicates
            // that market data is restored, which we can interpret as restored connection.
            boolean isConnectivityRestored = (errorCode == 1101 || errorCode == 1102 || errorCode == 2104);
            if (isConnectivityRestored) {
                if (!traderAssistant.getOpenOrders().isEmpty()) {
                    eventReport.report(JArbitrager.APP_NAME, "Checking for executions while TWS was disconnected from the IB server.");
                    traderAssistant.requestExecutions();
                }
            }

            // Error 322 occurs from time to time when the first order is submitted. The cause is unknown,
            // it's assumed to be a bug in the IB API. When this error is generated, the order is rejected
            // with a message such as this: Error processing request:-'ub' : cause - jextend.ub.f(ub.java:1193)
            // To get around this problem, we simply request executions for open orders. If the order execution
            // is not found, another order would be submitted.
            if (errorCode == 322) {
                if (!traderAssistant.getOpenOrders().isEmpty()) {
                    eventReport.report(JArbitrager.APP_NAME, "Checking for executions after error 322.");
                    traderAssistant.requestExecutions();
                }
            }

            // 200: bad contract
            boolean isInvalidRequest = (errorCode == 200);
            if (isInvalidRequest) {
                Dispatcher.getInstance().fireModelChanged(ModelListener.Event.Error, "IB reported: " + errorMsg);
            }

            //todo ekk handle error 201


        } catch (Throwable t) {
            // Do not allow exceptions come back to the socket -- it will cause disconnects
            eventReport.report(t);
        }
    }

    @Override
    public void tickPrice(int tickerId, int field, double price, int canAutoExecute) {
        try {
            if (price > 0) {
                if (field == TickType.BID || field == TickType.ASK) {
                    Strategy strategy = traderAssistant.getStrategyForTicker(tickerId);
                    Instrument instrument = strategy.getInstrumentForTicker(tickerId);
                    if (field == TickType.BID) {
                        System.out.println("ticker: " + tickerId + ", bid " + price);
                        instrument.setBid(price);
                    }

                    if (field == TickType.ASK) {
                        System.out.println("ticker: " + tickerId + ", ask " + price);
                        instrument.setAsk(price);
                    }

                    strategy.setTime(System.currentTimeMillis());
                    strategy.process();
                    Dispatcher.getInstance().fireModelChanged(ModelListener.Event.StrategyUpdate, strategy);
                    strategy.saveSnapshot();
                }
            }
        } catch (Throwable t) {
            // Do not allow exceptions come back to the socket -- it will cause
            // disconnects
            eventReport.report(t);
        }
    }

    @Override
    public void nextValidId(int orderId) {
        traderAssistant.setOrderId(orderId);
    }

}
