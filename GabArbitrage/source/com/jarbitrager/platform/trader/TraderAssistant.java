package com.jarbitrager.platform.trader;

import com.ib.client.*;
import com.jarbitrager.platform.instrument.*;
import com.jarbitrager.platform.model.*;
import com.jarbitrager.platform.position.*;
import static com.jarbitrager.platform.preferences.JArbitragerPreferences.*;
import com.jarbitrager.platform.preferences.*;
import com.jarbitrager.platform.report.*;
import com.jarbitrager.platform.startup.*;
import com.jarbitrager.platform.strategy.*;

import javax.swing.*;
import java.util.*;

public class TraderAssistant {
    private final Map<Integer, Strategy> strategies;
    private final Map<Integer, OpenOrder> openOrders;
    private final EventReport eventReport;
    private final Trader trader;
    private final Dispatcher dispatcher;

    private EClientSocket socket;
    private int tickerId, orderId, strategyId, serverVersion;
    private String accountCode;// used to determine if TWS is running against real or paper trading account

    public TraderAssistant(Trader trader) {
        this.trader = trader;
        dispatcher = Dispatcher.getInstance();
        eventReport = dispatcher.getEventReport();
        strategies = new HashMap<Integer, Strategy>();
        openOrders = new HashMap<Integer, OpenOrder>();
    }

    public Map<Integer, OpenOrder> getOpenOrders() {
        return openOrders;
    }

    public Strategy getStrategy(int strategyId) {
        return strategies.get(strategyId);
    }

    public Collection<Strategy> getAllStrategies() {
        return strategies.values();
    }

    public Strategy getStrategyForTicker(int tickerId) {
        for (Strategy strategy : strategies.values()) {
            if (strategy.getInstrument1().getTickerId() == tickerId || strategy.getInstrument2().getTickerId() == tickerId) {
                return strategy;
            }
        }
        return null;
    }


    public Strategy getStrategy(String name) {
        Strategy strategy = null;
        for (Map.Entry<Integer, Strategy> mapEntry : strategies.entrySet()) {
            Strategy thisStrategy = mapEntry.getValue();
            if (thisStrategy.getName().equals(name)) {
                strategy = thisStrategy;
                break;
            }
        }
        return strategy;
    }

    public void connect() throws JArbitragerException {
        if (socket == null || !socket.isConnected()) {
            eventReport.report(JArbitrager.APP_NAME, "Connecting to TWS");

            socket = new EClientSocket(trader);
            PreferencesHolder prefs = PreferencesHolder.getInstance();
            String host = prefs.get(Host);
            int port = prefs.getInt(Port);
            int clientID = prefs.getInt(ClientID);

            socket.eConnect(host, port, clientID);
            if (!socket.isConnected()) {
                throw new JArbitragerException("Could not connect to TWS. See report for details.");
            }

            // IB Log levels: 1=SYSTEM 2=ERROR 3=WARNING 4=INFORMATION 5=DETAIL
            socket.setServerLogLevel(3);
            socket.reqNewsBulletins(true);
            serverVersion = socket.serverVersion();
            eventReport.report(JArbitrager.APP_NAME, "Connected to TWS");
            checkAccountType();
        }
    }

    public int getServerVersion() {
        return serverVersion;
    }

    public void disconnect() {
        if (socket != null && socket.isConnected()) {
            socket.cancelNewsBulletins();
            socket.eDisconnect();
        }
    }

    /**
     * While TWS was disconnected from the IB server, some order executions may have occurred.
     * To detect executions, request them explicitly after the reconnection.
     */
    public void requestExecutions() {
        try {
            for (OpenOrder openOrder : openOrders.values()) {
                openOrder.reset();
                eventReport.report(JArbitrager.APP_NAME, "Requesting executions for open order " + openOrder.getId());
                socket.reqExecutions(openOrder.getId(), new ExecutionFilter());
            }
        } catch (Throwable t) {
            // Do not allow exceptions come back to the socket -- it will cause disconnects
            eventReport.report(t);
        }
    }

    private synchronized void requestMarketData(Strategy strategy) {
        tickerId++;
        Instrument instrument1 = strategy.getInstrument1();
        instrument1.setTickerId(tickerId);
        Contract contract1 = instrument1.getContract();
        socket.reqMktData(tickerId, contract1, "", false);

        tickerId++;
        Instrument instrument2 = strategy.getInstrument2();
        instrument2.setTickerId(tickerId);
        Contract contract2 = instrument2.getContract();
        socket.reqMktData(tickerId, contract2, "", false);

        eventReport.report(strategy.getName(), "Requested market data");
        socket.reqContractDetails(contract1.m_conId, contract1);
        socket.reqContractDetails(contract2.m_conId, contract2);
    }

    public synchronized void addStrategy(Strategy strategy) {
        strategy.getIndicatorManager().setInstruments(strategy);
        strategyId++;
        strategies.put(strategyId, strategy);
        Mode mode = dispatcher.getMode();
        if (mode == Mode.ForwardTest || mode == Mode.Trade) {
            String msg = "strategy started. " + strategy.getTradingSchedule();
            eventReport.report(strategy.getName(), msg);
            requestMarketData(strategy);
            strategy.setIsActive(true);
            dispatcher.strategyStarted();
        }
    }

    public synchronized void removeAllStrategies() {
        strategies.clear();
        openOrders.clear();
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    private synchronized void placeOrder(Instrument instrument, Order order, Strategy strategy) {
        try {
            orderId++;
            Mode mode = dispatcher.getMode();
            if (mode == Mode.Trade || mode == Mode.ForwardTest) {
                String msg = "Placing order " + orderId + " for " + instrument;
                eventReport.report(strategy.getName(), msg);
            }

            openOrders.put(orderId, new OpenOrder(orderId, order, strategy, instrument));

            if (mode == Mode.Trade) {
                socket.placeOrder(orderId, instrument.getContract(), order);
            } else {
                Execution execution = new Execution();
                execution.m_shares = order.m_totalQuantity;
                execution.m_price = order.m_action.equalsIgnoreCase("BUY") ? instrument.getAsk() : instrument.getBid();
                execution.m_orderId = orderId;
                trader.execDetails(0, instrument.getContract(), execution);
            }
        } catch (Throwable t) {
            // Do not allow exceptions come back to the socket -- it will cause disconnects
            eventReport.report(t);
        }

    }

    public void placeMarketOrder(Instrument instrument, int quantity, String action, Strategy strategy) {
        Order order = new Order();
        order.m_action = action;
        order.m_totalQuantity = quantity;
        order.m_orderType = "MKT";
        placeOrder(instrument, order, strategy);
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    private void checkAccountType() throws JArbitragerException {
        socket.reqAccountUpdates(true, "");

        try {
            synchronized (trader) {
                while (accountCode == null) {
                    trader.wait();
                }
            }
        } catch (InterruptedException ie) {
            throw new JArbitragerException(ie);
        }

        socket.reqAccountUpdates(false, "");
        boolean isRealTrading = !accountCode.startsWith("D") && dispatcher.getMode() == Mode.Trade;
        if (isRealTrading) {
            String lineSep = System.getProperty("line.separator");
            String warning = "Connected to a real (not simulated) IB account. ";
            warning += "Running " + JArbitrager.APP_NAME + " in trading mode against a real" + lineSep;
            warning += "account may cause significant losses in your account. ";
            warning += "Are you sure you want to proceed?";
            int response = JOptionPane.showConfirmDialog(null, warning, JArbitrager.APP_NAME, JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.NO_OPTION) {
                disconnect();
            }
        }
    }

}
