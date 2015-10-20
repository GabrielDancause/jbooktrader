package com.jarbitrager.platform.model;

public interface ModelListener {
    enum Event {
        StrategiesStart, StrategiesEnd, StrategyUpdate, ModeChanged, Error
    }

    public void modelChanged(Event event, Object value);
}
