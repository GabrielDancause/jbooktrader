package com.jarbitrager.indicator;

import com.jarbitrager.platform.indicator.*;
import com.jarbitrager.platform.util.*;

/**
 * Difference in price between the two instruments.
 */
public class SmaDifferential extends Indicator {

    private double magnifier;
    private final MovingWindow diffs;

    public SmaDifferential(int period) {
        diffs = new MovingWindow(period);
    }

    @Override
    public void calculate() {

        double price1 = instrument1.getMidPrice();
        double price2 = instrument2.getMidPrice();

        if (magnifier == 0) {
            magnifier = price1 / price2;
        }

        double diff = price1 - magnifier * price2;
        diffs.add(diff);
        if (diffs.isFull()) {
            double mean = diffs.getMean();
            value = diff - mean;
        }

    }

    @Override
    public void reset() {
        value = magnifier = 0;
        diffs.clear();
    }

}

