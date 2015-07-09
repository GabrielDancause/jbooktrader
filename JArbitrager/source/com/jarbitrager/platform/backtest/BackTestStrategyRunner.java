package com.jarbitrager.platform.backtest;

import com.jarbitrager.platform.model.*;
import com.jarbitrager.platform.strategy.*;
import com.jarbitrager.platform.util.*;

/**
 * Runs a trading strategy in the optimizer mode using a data file containing
 * historical market snapshots.
 */
public class BackTestStrategyRunner implements Runnable {
    private final BackTestDialog backTestDialog;
    private final Strategy strategy;
    private BackTestFileReader backTestFileReader;
    private BackTester backTester;

    public BackTestStrategyRunner(BackTestDialog backTestDialog, Strategy strategy) {
        this.backTestDialog = backTestDialog;
        this.strategy = strategy;
        Dispatcher.getInstance().getTrader().getAssistant().addStrategy(strategy);
    }

    public void cancel() {
        backTestFileReader.cancel();
        if (backTester != null) {
            backTester.cancel();
        }
        backTestDialog.showProgress("Stopping back test...");
    }

    public void run() {
        try {
            backTestDialog.enableProgress();
            backTestFileReader = new BackTestFileReader(backTestDialog.getFileName());
            backTestDialog.showProgress("Scanning historical data file...");
            backTestFileReader.scan(strategy.getInstruments());

            backTestDialog.showProgress("Running back test...");
            backTester = new BackTester(strategy, backTestFileReader, backTestDialog);
            backTester.execute();
        } catch (Throwable t) {
            MessageDialog.showError(t);
        } finally {
            backTestDialog.dispose();
        }
    }
}
