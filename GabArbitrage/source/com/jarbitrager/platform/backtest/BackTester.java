package com.jarbitrager.platform.backtest;

import com.jarbitrager.platform.chart.*;
import com.jarbitrager.platform.indicator.*;
import com.jarbitrager.platform.instrument.*;
import com.jarbitrager.platform.model.*;
import com.jarbitrager.platform.position.*;
import com.jarbitrager.platform.strategy.*;

/**
 * This class is responsible for running the strategy against historical market
 * data
 */
public class BackTester {
    private final Strategy strategy;
    private final BackTestFileReader backTestFileReader;
    private final BackTestDialog backTestDialog;
    private final Dispatcher dispatcher;
    private boolean isCanceled;

    public BackTester(Strategy strategy, BackTestFileReader backTestFileReader, BackTestDialog backTestDialog) {
        dispatcher = Dispatcher.getInstance();
        this.strategy = strategy;
        this.backTestFileReader = backTestFileReader;
        this.backTestDialog = backTestDialog;
    }

    public void cancel() {
        isCanceled = true;
    }

    public void execute() {
        strategy.setIsActive(true);
        PositionManager positionManager = strategy.getPositionManager();
        IndicatorManager indicatorManager = strategy.getIndicatorManager();
        PerformanceChartData performanceChartData = strategy.getPerformanceChartData();

        long snapshotCounter = 0;
        long size = backTestFileReader.getSnapshotCount();

        MarketSnapshot marketSnapshot;

        while (!isCanceled && (marketSnapshot = backTestFileReader.next()) != null) {
            snapshotCounter++;
            strategy.setSnapshot(marketSnapshot);
            strategy.process();

            performanceChartData.updatePrices(marketSnapshot);
            performanceChartData.updateIndicators(indicatorManager.getIndicators(), strategy.getTime());

            if (snapshotCounter % 10000 == 0) {
                backTestDialog.setProgress(snapshotCounter, size);
            }
        }

        if (!isCanceled) {
            // go flat at the end of the test period to finalize the run
            strategy.closePosition();
            positionManager.trade();
            strategy.setIsActive(false);
            dispatcher.fireModelChanged(ModelListener.Event.StrategyUpdate, strategy);
        }
    }
}
