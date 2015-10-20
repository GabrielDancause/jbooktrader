package com.jarbitrager.platform.position;

import com.ib.client.*;
import com.jarbitrager.platform.instrument.*;
import com.jarbitrager.platform.strategy.*;

/**
 * Encapsulates the order execution information.
 */
public class OpenOrder {
    private final int id;
    private final Order order;
    private final Strategy strategy;
    private final Instrument instrument;
    private long date;
    private int sharesFilled;
    private boolean isFilled;
    private double avgFillPrice;

    public OpenOrder(int id, Order order, Strategy strategy, Instrument instrument) {
        this.id = id;
        this.order = order;
        this.strategy = strategy;
        this.instrument = instrument;
    }

    public int getId() {
        return id;
    }

    public Order getOrder() {
        return order;
    }

    public void add(Execution execution) {
        sharesFilled += execution.m_shares;
        avgFillPrice += execution.m_price * execution.m_shares;

        if (sharesFilled == order.m_totalQuantity) {
            avgFillPrice /= sharesFilled;
            date = strategy.getTime();
            isFilled = true;
        }
    }

    public void reset() {
        sharesFilled = 0;
        avgFillPrice = 0;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public boolean isFilled() {
        return isFilled;
    }

    public int getSharesFilled() {
        return sharesFilled;
    }

    public double getAvgFillPrice() {
        return avgFillPrice;
    }

    public long getDate() {
        return date;
    }

}
