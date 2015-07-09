package com.jarbitrager.platform.indicator;

import com.jarbitrager.platform.instrument.*;

/**
 * Base class for all classes implementing technical indicators.
 */
public abstract class Indicator {
    private final String name;
    protected Instrument instrument1, instrument2;
    protected double value;

    public abstract void calculate();

    public abstract void reset();

    protected Indicator() {
        name = getClass().getSimpleName();
    }

    public void setInstruments(Instrument instrument1, Instrument instrument2) {
        this.instrument1 = instrument1;
        this.instrument2 = instrument2;
    }

    public boolean hasNormalBidAskSpreads() {
        return instrument1.hasNormalBidAskSpread() && instrument2.hasNormalBidAskSpread();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" value: ").append(value);
        return sb.toString();
    }

    public double getValue() {
        return value;
    }

    public String getName() {
        return name;
    }
}
