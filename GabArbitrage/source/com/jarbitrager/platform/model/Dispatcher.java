package com.jarbitrager.platform.model;

import com.jarbitrager.platform.report.*;
import com.jarbitrager.platform.startup.*;
import com.jarbitrager.platform.trader.*;
import com.jarbitrager.platform.web.*;

import java.util.*;

/**
 * Acts as the dispatcher of the services.
 */
public class Dispatcher {
    private static Dispatcher instance;
    private final List<ModelListener> listeners;
    private EventReport eventReport;
    private Trader trader;
    private Mode mode;
    private int activeStrategies;

    private Dispatcher() {
        listeners = new ArrayList<ModelListener>();
    }

    public static synchronized Dispatcher getInstance() {
        if (instance == null) {
            instance = new Dispatcher();
        }
        return instance;
    }

    public void setReporter() throws JArbitragerException {
        eventReport = new EventReport();
    }

    public void addListener(ModelListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ModelListener listener) {
        listeners.remove(listener);
    }

    public void fireModelChanged(ModelListener.Event event, Object value) {
        if (mode != Mode.Optimization) {
            for (ModelListener listener : listeners) {
                try {
                    listener.modelChanged(event, value);
                } catch (Exception e) {
                    eventReport.report(e);
                }
            }
        }
    }

    public void fireModelChanged(ModelListener.Event event) {
        fireModelChanged(event, null);
    }


    synchronized public Trader getTrader() {
        if (trader == null) {
            trader = new Trader();
        }
        return trader;
    }

    public EventReport getEventReport() {
        return eventReport;
    }

    public Mode getMode() {
        return mode;
    }

    public void exit() {
        if (trader != null) {
            trader.getAssistant().disconnect();
        }
        System.exit(0);
    }

    public void setMode(Mode mode) throws JArbitragerException {

        if (this.mode != mode) {
            eventReport.report(JArbitrager.APP_NAME, "Running mode changed to: " + mode.getName());
        }

        this.mode = mode;

        // Disable all reporting when JA runs in optimization mode. The optimizer
        // runs thousands of strategies, and the amount of data to report would be enormous.
        if (mode == Mode.Optimization) {
            eventReport.disable();
        } else {
            eventReport.enable();
        }

        if (mode == Mode.Trade || mode == Mode.ForwardTest) {
            trader.getAssistant().connect();
            MonitoringServer.start();
        } else {
            trader.getAssistant().disconnect();
        }

        fireModelChanged(ModelListener.Event.ModeChanged);
    }

    public synchronized void strategyStarted() {
        activeStrategies++;
        fireModelChanged(ModelListener.Event.StrategiesStart);
    }

    public synchronized void strategyCompleted() {
        activeStrategies--;
        if (activeStrategies == 0) {
            fireModelChanged(ModelListener.Event.StrategiesEnd);
        }
    }
}
