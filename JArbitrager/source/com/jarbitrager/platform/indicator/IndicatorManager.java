package com.jarbitrager.platform.indicator;

import com.jarbitrager.platform.instrument.*;
import com.jarbitrager.platform.strategy.*;

import java.util.*;

/**
 *
 */
public class IndicatorManager {
    private static final long GAP_SIZE = 60 * 60 * 1000;// 1 hour
    private final List<Indicator> indicators;
    private boolean hasValidIndicators;
    private long lastUpdateTime;

    public IndicatorManager() {
        indicators = new LinkedList<Indicator>();
    }

    public void setInstruments(Strategy strategy) {
        Instrument instrument1 = strategy.getInstrument1();
        Instrument instrument2 = strategy.getInstrument2();
        for (Indicator indicator : indicators) {
            indicator.setInstruments(instrument1, instrument2);
        }
    }

    public boolean hasValidIndicators() {
        return hasValidIndicators;
    }

    public void addIndicator(Indicator indicator) {
        indicators.add(indicator);
    }

    public List<Indicator> getIndicators() {
        return indicators;
    }

    public void updateIndicators(long updateTime) {
        hasValidIndicators = true;
        long iterations = Math.max(1, (updateTime - lastUpdateTime) / 1000);

        if (updateTime - lastUpdateTime > GAP_SIZE) {
            iterations = 1;
            for (Indicator indicator : indicators) {
                indicator.reset();
            }
        }
        lastUpdateTime = updateTime;

        for (Indicator indicator : indicators) {
            try {
                if (indicator.hasNormalBidAskSpreads()) {
                    for (int iteration = 0; iteration < iterations; iteration++) {
                        indicator.calculate();
                    }
                }
            } catch (IndexOutOfBoundsException iobe) {
                hasValidIndicators = false;
                // This exception will occur if book size is insufficient
                // to calculate the indicator. This is normal.
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
