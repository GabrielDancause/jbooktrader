package com.jarbitrager.platform.instrument;

import com.ib.client.*;
import com.jarbitrager.platform.commission.*;

/**
 */
public class Instrument {
    private final Contract contract;
    private final Commission commission;
    private double bid, ask, aveFillPrice, tradeCommission;
    private final int multiplier;
    private int currentPosition, targetPosition;
    private double bidAskSpread;
    private long samples;
    private int tickerId;

    public Instrument(Contract contract, Commission commission, int multiplier) {
        this.contract = contract;
        this.commission = commission;
        this.multiplier = multiplier;
    }

    public void setTickerId(int tickerId) {
        this.tickerId = tickerId;
    }

    public int getTickerId() {
        return tickerId;
    }


    public String toString() {
        String s = contract.m_symbol + "-" + contract.m_exchange;
        if (contract.m_currency != null) {
            s += "-" + contract.m_currency;
        }
        return s;
    }

    private void updateBidAskSpread() {
        if (isValid()) {
            samples++;
            bidAskSpread += (ask - bid);
        }
    }

    public boolean hasNormalBidAskSpread() {
        if (samples <= 0) {
            return false;
        }
        double averageBidAskSpread = bidAskSpread / samples;
        double currentSpread = ask - bid;
        return currentSpread > 0 && currentSpread <= 2. * averageBidAskSpread;
    }


    public void setBid(double bid) {
        this.bid = bid;
        updateBidAskSpread();
    }

    public void setAsk(double ask) {
        this.ask = ask;
        updateBidAskSpread();
    }

    public double getBid() {
        return bid;
    }

    public double getAsk() {
        return ask;
    }

    public double getMidPrice() {
        return (bid + ask) / 2;
    }

    public boolean isValid() {
        return (bid > 0 && ask > 0 && bid < ask);
    }

    public Contract getContract() {
        return contract;
    }

    public Commission getCommission() {
        return commission;
    }

    public double getTradeCommission() {
        return tradeCommission;
    }

    public void setTradeCommission(double tradeCommission) {
        this.tradeCommission = tradeCommission;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public boolean hasCompletedTrade() {
        return currentPosition == targetPosition;
    }


    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    public int getTargetPosition() {
        return targetPosition;
    }

    public void setTargetPosition(int targetPosition) {
        this.targetPosition = targetPosition;
    }

    public double getAveFillPrice() {
        return aveFillPrice;
    }

    public void setAveFillPrice(double aveFillPrice) {
        this.aveFillPrice = aveFillPrice;
    }
}
