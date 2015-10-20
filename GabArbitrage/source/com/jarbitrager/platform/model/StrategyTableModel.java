package com.jarbitrager.platform.model;

import static com.jarbitrager.platform.model.StrategyTableColumn.*;
import com.jarbitrager.platform.performance.*;
import com.jarbitrager.platform.strategy.*;
import com.jarbitrager.platform.trader.*;
import com.jarbitrager.platform.util.*;

import javax.swing.*;
import java.util.*;

/**
 */
public class StrategyTableModel extends TableDataModel {
    private final TraderAssistant traderAssistant;

    public StrategyTableModel() {
        StrategyTableColumn[] columns = StrategyTableColumn.values();
        ArrayList<String> allColumns = new ArrayList<String>();
        for (StrategyTableColumn column : columns) {
            allColumns.add(column.getColumnName());
        }
        setSchema(allColumns.toArray(new String[columns.length]));
        traderAssistant = Dispatcher.getInstance().getTrader().getAssistant();
    }

    @Override
    public Class<?> getColumnClass(int col) {
        StrategyTableColumn column = StrategyTableColumn.values()[col];
        return column.getColumnClass();
    }

    public String getStrategyNameForRow(int row) {
        return (String) getRow(row)[Strategy.ordinal()];
    }

    public Strategy getStrategyForRow(int row) {
        String name = getStrategyNameForRow(row);
        return traderAssistant.getStrategy(name);
    }

    public Strategy createStrategyForRow(int row) throws JArbitragerException {
        Strategy strategy = getStrategyForRow(row);
        if (strategy != null && strategy.isActive()) {
            throw new JArbitragerException("Strategy " + strategy + " is already running.");
        }
        String strategyName = getStrategyNameForRow(row);
        strategy = ClassFinder.getInstance(strategyName);
        update(strategy);
        fireTableRowsUpdated(row, row);
        return strategy;
    }

    private int getRowForStrategy(Strategy strategy) {
        int selectedRow = -1;
        int rowCount = getRowCount();
        for (int row = 0; row < rowCount; row++) {
            String name = getStrategyNameForRow(row);
            if (name.equals(strategy.getName())) {
                selectedRow = row;
                break;
            }
        }
        return selectedRow;
    }

    public void update(Strategy strategy) {
        final int row = getRowForStrategy(strategy);

        setValueAt(strategy.getInstrument1().getBid(), row, Bid1.ordinal());
        setValueAt(strategy.getInstrument1().getAsk(), row, Ask1.ordinal());
        setValueAt(strategy.getInstrument1().getCurrentPosition(), row, Position1.ordinal());
        setValueAt(strategy.getInstrument2().getBid(), row, Bid2.ordinal());
        setValueAt(strategy.getInstrument2().getAsk(), row, Ask2.ordinal());
        setValueAt(strategy.getInstrument2().getCurrentPosition(), row, Position2.ordinal());

        PerformanceManager performanceManager = strategy.getPerformanceManager();
        setValueAt(strategy.indicatorsState(), row, Indicators.ordinal());
        setValueAt(performanceManager.getTrades(), row, Trades.ordinal());
        setValueAt(performanceManager.getMaxDrawdown(), row, MaxDD.ordinal());
        setValueAt(performanceManager.getNetProfit(), row, NetProfit.ordinal());
        setValueAt(performanceManager.getProfitFactor(), row, ProfitFactor.ordinal());
        setValueAt(performanceManager.getPerformanceIndex(), row, PerformanceIndex.ordinal());

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                fireTableRowsUpdated(row, row);
            }
        });
    }

    public void addStrategy(Strategy strategy) {
        Object[] row = new Object[getColumnCount()];
        row[Strategy.ordinal()] = strategy.getName();
        row[Symbol1.ordinal()] = strategy.getInstrument1().getContract().m_symbol;
        row[Symbol2.ordinal()] = strategy.getInstrument2().getContract().m_symbol;
        addRow(row);
    }
}
