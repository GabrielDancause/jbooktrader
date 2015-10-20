package com.jarbitrager.indicator;

import com.jarbitrager.platform.indicator.*;

/**
 * Difference in price between the two instruments.
 */
public class EmaDifferential extends Indicator {
    private final double multiplier;
    private double magnifier, ema;

    public EmaDifferential(int periodLength) {
        multiplier = 2. / (periodLength + 1.);
    }

    @Override
    public void calculate() {

        double price1 = instrument1.getMidPrice();
        double price2 = instrument2.getMidPrice();

        if (magnifier == 0) {
            magnifier = price1 / price2;
        }

        double diff = price1 - magnifier * price2;
        ema += (diff - ema) * multiplier;
        value = diff - ema;
    }

    @Override
    public void reset() {
        value = ema = magnifier = 0;
    }

}

