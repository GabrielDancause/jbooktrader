package com.jarbitrager.platform.model;

public enum StrategyTableColumn {
    Strategy("Strategy", String.class),
    Symbol1("Symbol", String.class),
    Bid1("Bid", Double.class),
    Ask1("Ask", Double.class),
    Position1("Position", Integer.class),
    Symbol2("Symbol", String.class),
    Bid2("Bid", Double.class),
    Ask2("Ask", Double.class),
    Position2("Position", Integer.class),
    Indicators("Indicators", String.class),
    Trades("Trades", Integer.class),
    MaxDD("Max DD", Double.class),
    NetProfit("Net Profit", Double.class),
    ProfitFactor("PF", Double.class),
    PerformanceIndex("PI", Double.class);

    private final String columnName;
    private final Class<?> columnClass;

    StrategyTableColumn(String columnName, Class<?> columnClass) {
        this.columnName = columnName;
        this.columnClass = columnClass;
    }

    public String getColumnName() {
        return columnName;
    }

    public Class<?> getColumnClass() {
        return columnClass;
    }

}
