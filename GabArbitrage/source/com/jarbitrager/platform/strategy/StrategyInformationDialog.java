package com.jarbitrager.platform.strategy;

import com.ib.client.*;
import com.jarbitrager.platform.indicator.*;
import com.jarbitrager.platform.optimizer.*;
import com.jarbitrager.platform.performance.*;
import com.jarbitrager.platform.util.*;

import javax.swing.*;
import java.awt.*;
import java.text.*;

public final class StrategyInformationDialog extends JDialog {
    private final Strategy strategy;

    public StrategyInformationDialog(JFrame parent, Strategy strategy) {
        super(parent);
        this.strategy = strategy;
        init();
        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    private void add(JPanel panel, String fieldName, String fieldValue) {
        JLabel fieldNameLabel = new JLabel(fieldName + ":");
        JLabel fieldValueLabel = new JLabel(fieldValue);
        fieldValueLabel.setForeground(Color.BLACK);
        panel.add(fieldNameLabel);
        panel.add(fieldValueLabel);
    }

    private void add(JPanel panel, String fieldName, int fieldValue) {
        add(panel, fieldName, String.valueOf(fieldValue));
    }

    private void add(JPanel panel, String fieldName, double fieldValue) {
        add(panel, fieldName, String.valueOf(fieldValue));
    }

    private void makeCompactGrid(JPanel panel) {
        SpringUtilities.makeCompactGrid(panel, panel.getComponentCount() / 2, 2, 12, 12, 5, 5);
    }

    private void init() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("Strategy Information - " + strategy.getName());

        JPanel contentPanel = new JPanel(new BorderLayout());
        getContentPane().add(contentPanel, BorderLayout.CENTER);

        JTabbedPane instrumentsPane = new JTabbedPane();
        contentPanel.add(instrumentsPane, BorderLayout.CENTER);

        JPanel performancePanel = new JPanel(new SpringLayout());
        instrumentsPane.addTab("Performance", performancePanel);

        NumberFormat nf2 = NumberFormatterFactory.getNumberFormatter(2);

        PerformanceManager pm = strategy.getPerformanceManager();
        add(performancePanel, "Trades", pm.getTrades());
        add(performancePanel, "% Profitable", nf2.format(pm.getPercentProfitableTrades()));
        add(performancePanel, "Average trade", nf2.format(pm.getAverageProfitPerTrade()));
        add(performancePanel, "Net Profit", nf2.format(pm.getNetProfit()));
        add(performancePanel, "Max Drawdown", nf2.format(pm.getMaxDrawdown()));
        add(performancePanel, "Profit Factor", nf2.format(pm.getProfitFactor()));
        add(performancePanel, "Kelly", nf2.format(pm.getKellyCriterion()));
        add(performancePanel, "Perf. Index", nf2.format(pm.getPerformanceIndex()));
        makeCompactGrid(performancePanel);

        JPanel securityPanel = new JPanel(new SpringLayout());
        instrumentsPane.addTab("Instruments", securityPanel);
        Contract contract1 = strategy.getInstrument1().getContract();
        Contract contract2 = strategy.getInstrument2().getContract();

        add(securityPanel, "Symbol", contract1.m_symbol);
        add(securityPanel, "Security Type", contract1.m_secType);
        add(securityPanel, "Exchange", contract1.m_exchange);
        add(securityPanel, "Multiplier", contract1.m_multiplier);

        add(securityPanel, "Symbol", contract2.m_symbol);
        add(securityPanel, "Security Type", contract2.m_secType);
        add(securityPanel, "Exchange", contract2.m_exchange);
        add(securityPanel, "Multiplier", contract2.m_multiplier);

        makeCompactGrid(securityPanel);

        JPanel parametersPanel = new JPanel(new SpringLayout());
        instrumentsPane.addTab("Parameters", parametersPanel);
        StrategyParams params = strategy.getParams();
        add(parametersPanel, "Schedule", strategy.getTradingSchedule().toString());
        for (StrategyParam param : params.getAll()) {
            add(parametersPanel, param.getName(), param.getValue());
        }
        makeCompactGrid(parametersPanel);

        JPanel indicatorsPanel = new JPanel(new SpringLayout());
        instrumentsPane.addTab("Indicators", indicatorsPanel);
        for (Indicator indicator : strategy.getIndicatorManager().getIndicators()) {
            add(indicatorsPanel, indicator.getName(), indicator.getValue());
        }
        makeCompactGrid(indicatorsPanel);

        getContentPane().setPreferredSize(new Dimension(450, 400));
    }

}
