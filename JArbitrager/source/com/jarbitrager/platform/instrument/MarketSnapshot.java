package com.jarbitrager.platform.instrument;

/**
 */
public class MarketSnapshot {
    private final long time;
    private final double bid1, ask1, bid2, ask2;

    public MarketSnapshot(long time, double bid1, double ask1, double bid2, double ask2) {
        this.time = time;
        this.bid1 = bid1;
        this.ask1 = ask1;
        this.bid2 = bid2;
        this.ask2 = ask2;
    }

    public long getTime() {
        return time;
    }

    public double getBid1() {
        return bid1;
    }

    public double getBid2() {
        return bid2;
    }

    public double getAsk1() {
        return ask1;
    }

    public double getAsk2() {
        return ask2;
    }

    public double getMidPrice1() {
        return (bid1 + ask1) / 2;
    }

    public double getMidPrice2() {
        return (bid2 + ask2) / 2;
    }

    public String toString() {
        StringBuilder marketSnapshot = new StringBuilder();
        marketSnapshot.append("time: ").append(getTime());
        marketSnapshot.append(" mid price1: ").append(getMidPrice1());
        marketSnapshot.append(" mid price2: ").append(getMidPrice2());

        return marketSnapshot.toString();
    }

}
