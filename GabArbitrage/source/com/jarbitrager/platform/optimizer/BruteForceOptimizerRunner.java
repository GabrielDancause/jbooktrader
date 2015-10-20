package com.jarbitrager.platform.optimizer;

import com.jarbitrager.platform.model.*;
import com.jarbitrager.platform.strategy.*;

import java.util.*;

/**
 */
public class BruteForceOptimizerRunner extends OptimizerRunner {

    public BruteForceOptimizerRunner(OptimizerDialog optimizerDialog, Strategy strategy, StrategyParams params) throws JArbitragerException {
        super(optimizerDialog, strategy, params);
    }

    @Override
    public void optimize() throws JArbitragerException {
        Queue<StrategyParams> tasks = getTasks(strategyParams);
        int taskSize = tasks.size();
        setTotalSteps(snapshotCount * taskSize);
        setTotalStrategies(taskSize);
        execute(tasks);
    }
}
