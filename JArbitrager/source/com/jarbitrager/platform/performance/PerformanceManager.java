package com.jarbitrager.platform.performance;

import com.jarbitrager.platform.instrument.*;
import com.jarbitrager.platform.strategy.*;

/**
 * Performance manager evaluates trading strategy performance based on
 * statistics which include various factors, such as net profit, maximum
 * drawdown, profit factor, etc.
 */
public class PerformanceManager {
    private final Strategy strategy;
    private int trades, profitableTrades;
    private double tradeCommission, totalCommission;
    private double positionValue;
    private double totalBought, totalSold;
    private double tradeProfit, grossProfit, grossLoss, netProfit, netProfitAsOfPreviousTrade;
    private double peakNetProfit, maxDrawdown;
    private boolean isCompletedTrade;
    private double sumTradeProfit, sumTradeProfitSquared;

    public PerformanceManager(Strategy strategy) {
        this.strategy = strategy;
    }

    public int getTrades() {
        return trades;
    }

    public boolean getIsCompletedTrade() {
        return isCompletedTrade;
    }

    public double getPercentProfitableTrades() {
        return (trades == 0) ? 0 : (100d * profitableTrades / trades);
    }

    public double getAverageProfitPerTrade() {
        return (trades == 0) ? 0 : netProfit / trades;
    }

    public double getProfitFactor() {
        double profitFactor = 0;
        if (grossProfit > 0) {
            profitFactor = (grossLoss == 0) ? Double.POSITIVE_INFINITY : grossProfit / grossLoss;
        }
        return profitFactor;
    }

    public double getMaxDrawdown() {
        return maxDrawdown;
    }

    public double getTradeProfit() {
        return tradeProfit;
    }

    public double getTradeCommission() {
        return tradeCommission;
    }

    public double getNetProfit() {
        return totalSold - totalBought + positionValue - totalCommission;
    }

    public double getKellyCriterion() {
        int unprofitableTrades = trades - profitableTrades;
        if (profitableTrades > 0) {
            if (unprofitableTrades > 0) {
                double aveProfit = grossProfit / profitableTrades;
                double aveLoss = grossLoss / unprofitableTrades;
                double winLossRatio = aveProfit / aveLoss;
                double probabilityOfWin = profitableTrades / (double) trades;
                double kellyCriterion = probabilityOfWin - (1 - probabilityOfWin) / winLossRatio;
                kellyCriterion *= 100;
                return kellyCriterion;
            } else {
                return 100;
            }
        } else {
            return 0;
        }
    }

    public double getPerformanceIndex() {
        double pi = 0;
        if (trades > 0) {
            double stdev = Math.sqrt(trades * sumTradeProfitSquared - sumTradeProfit * sumTradeProfit) / trades;
            pi = (stdev == 0) ? Double.POSITIVE_INFINITY : Math.sqrt(trades) * getAverageProfitPerTrade() / stdev;
        }

        return pi;
    }

    public void updatePositionValue() {
        Instrument instrument1 = strategy.getInstrument1();
        Instrument instrument2 = strategy.getInstrument2();
        int position1 = instrument1.getCurrentPosition();
        int position2 = instrument2.getCurrentPosition();

        double price1 = position1 > 0 ? instrument1.getBid() : instrument1.getAsk();
        double price2 = position2 > 0 ? instrument2.getBid() : instrument2.getAsk();

        double value1 = position1 * price1 * instrument1.getMultiplier();
        double value2 = position2 * price2 * instrument2.getMultiplier();

        positionValue = value1 + value2;
    }

    public void updateOnTrade(int quantity, double avgFillPrice, Instrument instrument) {
        double tradeAmount = avgFillPrice * Math.abs(quantity) * instrument.getMultiplier();
        if (quantity > 0) {
            totalBought += tradeAmount;
        } else {
            totalSold += tradeAmount;
        }

        double commission = instrument.getCommission().getCommission(Math.abs(quantity), avgFillPrice);
        instrument.setTradeCommission(commission);

        if (strategy.hasCompletedTrade()) {
            isCompletedTrade = true;
            trades++;

            Instrument instrument1 = strategy.getInstrument1();
            Instrument instrument2 = strategy.getInstrument2();

            double value1 = instrument1.getCurrentPosition() * instrument1.getAveFillPrice() * instrument1.getMultiplier();
            double value2 = instrument2.getCurrentPosition() * instrument2.getAveFillPrice() * instrument2.getMultiplier();
            positionValue = value1 + value2;

            tradeCommission = instrument1.getTradeCommission() + instrument2.getTradeCommission();
            totalCommission += tradeCommission;

            netProfit = totalSold - totalBought + positionValue - totalCommission;
            peakNetProfit = Math.max(netProfit, peakNetProfit);
            maxDrawdown = Math.max(maxDrawdown, peakNetProfit - netProfit);

            tradeProfit = netProfit - netProfitAsOfPreviousTrade;
            netProfitAsOfPreviousTrade = netProfit;

            sumTradeProfit += tradeProfit;
            sumTradeProfitSquared += (tradeProfit * tradeProfit);

            if (tradeProfit >= 0) {
                profitableTrades++;
                grossProfit += tradeProfit;
            } else {
                grossLoss += (-tradeProfit);
            }
        } else {
            isCompletedTrade = false;
        }
    }
}
