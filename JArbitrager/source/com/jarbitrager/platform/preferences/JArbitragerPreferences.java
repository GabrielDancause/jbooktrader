package com.jarbitrager.platform.preferences;

public enum JArbitragerPreferences {
    // TWS connection
    Host("Host", "localhost"),
    Port("Port", "7496"),
    ClientID("Client ID", "1"),

    // Web Access
    WebAccess("Web access", "disabled"),
    WebAccessPort("Web access port", "1234"),
    WebAccessUser("Web access user", "admin"),
    WebAccessPassword("Web access password", "admin"),

    // Back tester
    BackTesterFileName("backTester.dataFileName", ""),

    // Optimizer
    OptimizerMinTrades("optimizer.minTrades", "50"),
    OptimizerSelectBy("optimizer.selectBy", ""),
    OptimizerMethod("optimizer.method", ""),
    OptimizerWindowWidth("optimizerwindow.width", "-1"),
    OptimizerWindowHeight("optimizerwindow.height", "-1"),
    OptimizerWindowX("optimizerwindow.x", "-1"),
    OptimizerWindowY("optimizerwindow.y", "-1"),

    // Main window
    MainWindowWidth("mainwindow.width", "-1"),
    MainWindowHeight("mainwindow.height", "-1"),
    MainWindowX("mainwindow.x", "-1"),
    MainWindowY("mainwindow.y", "-1"),

    // Performance chart
    PerformanceChartWidth("performance.chart.width", "-1"),
    PerformanceChartHeight("performance.chart.height", "-1"),
    PerformanceChartX("performance.chart.x", "-1"),
    PerformanceChartY("performance.chart.y", "-1"),
    PerformanceChartState("performance.chart.state", "-1"),

    // Optimizer
    DivideAndConquerCoverage("Divide & Conquer coverage", "10"),
    StrategiesPerProcessor("Strategies per processor", "50"),

    // Optimization Map
    OptimizationMapWidth("optimization.map.width", "-1"),
    OptimizationMapHeight("optimization.map.height", "-1"),
    OptimizationMapX("optimization.map.x", "-1"),
    OptimizationMapY("optimization.map.y", "-1");

    private final String name, defaultValue;

    private JArbitragerPreferences(String name, String defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    public String getDefault() {
        return defaultValue;
    }

    public String getName() {
        return name;
    }
}
