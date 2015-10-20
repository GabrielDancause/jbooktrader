package com.jarbitrager.platform.optimizer;

import com.jarbitrager.platform.instrument.*;
import com.jarbitrager.platform.model.*;
import com.jarbitrager.platform.performance.*;
import com.jarbitrager.platform.preferences.*;
import com.jarbitrager.platform.schedule.*;
import com.jarbitrager.platform.strategy.*;

import java.util.*;
import java.util.concurrent.*;

/**
 */
public class OptimizerWorker implements Callable<List<OptimizationResult>> {
    private final OptimizerRunner optimizerRunner;
    private final Queue<StrategyParams> tasks;

    public OptimizerWorker(OptimizerRunner optimizerRunner, Queue<StrategyParams> tasks) {
        this.optimizerRunner = optimizerRunner;
        this.tasks = tasks;
    }


    public List<OptimizationResult> call() throws JArbitragerException {
        List<Strategy> strategies = new ArrayList<Strategy>();
        List<OptimizationResult> optimizationResults = new ArrayList<OptimizationResult>(strategies.size());
        int strategiesPerProcessor = PreferencesHolder.getInstance().getInt(JArbitragerPreferences.StrategiesPerProcessor);

        while (!tasks.isEmpty()) {
            strategies.clear();
            while (strategies.size() < strategiesPerProcessor && !tasks.isEmpty()) {
                StrategyParams params = tasks.poll();
                if (params != null) {
                    Strategy strategy = optimizerRunner.getStrategyInstance(params);
                    strategies.add(strategy);
                }
            }


            if (!strategies.isEmpty()) {
                TradingSchedule tradingSchedule = strategies.get(0).getTradingSchedule();

                for (Strategy strategy : strategies) {
                    strategy.getIndicatorManager().setInstruments(strategy);
                }

                List<MarketSnapshot> snapshots = optimizerRunner.getSnapshots();
                for (MarketSnapshot marketSnapshot : snapshots) {
                    long time = marketSnapshot.getTime();
                    boolean isInSchedule = tradingSchedule.contains(time);

                    for (Strategy strategy : strategies) {
                        strategy.setSnapshot(marketSnapshot);
                        strategy.process(isInSchedule);
                    }

                    optimizerRunner.iterationsCompleted(strategies.size());
                    if (optimizerRunner.isCancelled()) {
                        return optimizationResults;
                    }
                }


                optimizationResults.clear();
                int minTrades = optimizerRunner.getMinTrades();


                for (Strategy strategy : strategies) {
                    strategy.closePosition();
                    strategy.getPositionManager().trade();


                    PerformanceManager performanceManager = strategy.getPerformanceManager();
                    int trades = performanceManager.getTrades();
                    if (trades >= minTrades) {
                        OptimizationResult optimizationResult = new OptimizationResult(strategy.getParams(), performanceManager);
                        optimizationResults.add(optimizationResult);
                    }
                }

                optimizerRunner.addResults(optimizationResults);
            }
        }

        return optimizationResults;
    }
}
