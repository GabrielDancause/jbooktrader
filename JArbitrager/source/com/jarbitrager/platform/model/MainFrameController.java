package com.jarbitrager.platform.model;

import com.jarbitrager.platform.backtest.*;
import com.jarbitrager.platform.chart.*;
import com.jarbitrager.platform.dialog.*;
import com.jarbitrager.platform.optimizer.*;
import static com.jarbitrager.platform.preferences.JArbitragerPreferences.*;
import com.jarbitrager.platform.preferences.*;
import com.jarbitrager.platform.startup.*;
import com.jarbitrager.platform.strategy.*;
import com.jarbitrager.platform.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Acts as a controller in the Model-View-Controller pattern
 */
public class MainFrameController {
    private final MainFrameDialog mainViewDialog;
    private final JTable strategyTable;
    private final StrategyTableModel strategyTableModel;
    private final PreferencesHolder prefs = PreferencesHolder.getInstance();
    private final Dispatcher dispatcher;

    public MainFrameController() throws JArbitragerException {
        mainViewDialog = new MainFrameDialog();
        dispatcher = Dispatcher.getInstance();
        dispatcher.addListener(mainViewDialog);
        int width = prefs.getInt(MainWindowWidth);
        int height = prefs.getInt(MainWindowHeight);
        int x = prefs.getInt(MainWindowX);
        int y = prefs.getInt(MainWindowY);

        if (width > 0 && height > 0) {
            mainViewDialog.setBounds(x, y, width, height);
        }

        strategyTable = mainViewDialog.getStrategyTable();
        strategyTableModel = mainViewDialog.getStrategyTableModel();
        assignListeners();
    }

    private void exit() {
        String question = "Are you sure you want to exit " + JArbitrager.APP_NAME + "?";
        int answer = JOptionPane.showConfirmDialog(mainViewDialog, question, JArbitrager.APP_NAME, JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            prefs.set(MainWindowWidth, mainViewDialog.getSize().width);
            prefs.set(MainWindowHeight, mainViewDialog.getSize().height);
            prefs.set(MainWindowX, mainViewDialog.getX());
            prefs.set(MainWindowY, mainViewDialog.getY());
            dispatcher.exit();
        }
    }

    private Strategy createSelectedRowStrategy() throws JArbitragerException {
        int selectedRow = strategyTable.getSelectedRow();
        if (selectedRow < 0) {
            throw new JArbitragerException("No strategy is selected.");
        }
        return strategyTableModel.createStrategyForRow(selectedRow);
    }

    private void openURL(String url) {
        try {
            Browser.openURL(url);
        } catch (Throwable t) {
            dispatcher.getEventReport().report(t);
            MessageDialog.showError(t);
        }
    }

    private void assignListeners() {

        strategyTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int modifiers = e.getModifiers();
                boolean actionRequested = (modifiers & InputEvent.BUTTON2_MASK) != 0;
                actionRequested = actionRequested || (modifiers & InputEvent.BUTTON3_MASK) != 0;
                if (actionRequested) {
                    int selectedRow = strategyTable.rowAtPoint(e.getPoint());
                    strategyTable.setRowSelectionInterval(selectedRow, selectedRow);
                    mainViewDialog.showPopup(e);
                    strategyTable.setRowSelectionInterval(selectedRow, selectedRow);
                }
            }
        });

        mainViewDialog.informationAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                    int selectedRow = strategyTable.getSelectedRow();
                    if (selectedRow < 0) {
                        throw new JArbitragerException("No strategy is selected.");
                    }

                    Strategy strategy = strategyTableModel.getStrategyForRow(selectedRow);
                    if (strategy == null) {
                        String name = strategyTableModel.getStrategyNameForRow(selectedRow);
                        strategy = ClassFinder.getInstance(name);
                    }

                    new StrategyInformationDialog(mainViewDialog, strategy);
                } catch (Throwable t) {
                    MessageDialog.showError(t);
                } finally {
                    mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });

        mainViewDialog.backTestAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    dispatcher.getTrader().getAssistant().removeAllStrategies();
                    Strategy strategy = createSelectedRowStrategy();
                    dispatcher.setMode(Mode.BackTest);
                    new BackTestDialog(mainViewDialog, strategy);
                } catch (Throwable t) {
                    MessageDialog.showError(t);
                } finally {
                    mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });

        mainViewDialog.optimizeAction(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                OptimizerDialog optimizerDialog = null;
                try {
                    mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    int selectedRow = strategyTable.getSelectedRow();
                    if (selectedRow < 0) {
                        throw new JArbitragerException("No strategy is selected.");
                    }
                    String name = strategyTableModel.getStrategyNameForRow(selectedRow);
                    dispatcher.setMode(Mode.Optimization);
                    optimizerDialog = new OptimizerDialog(mainViewDialog, name);

                    PreferencesHolder prefs = PreferencesHolder.getInstance();
                    int width = prefs.getInt(OptimizerWindowWidth);
                    int height = prefs.getInt(OptimizerWindowHeight);
                    int x = prefs.getInt(OptimizerWindowX);
                    int y = prefs.getInt(OptimizerWindowY);

                    if (width > 0 && height > 0) {
                        optimizerDialog.setBounds(x, y, width, height);
                    }
                    optimizerDialog.setVisible(true);
                } catch (Throwable t) {
                    MessageDialog.showError(t);
                } finally {
                    if (optimizerDialog != null) {
                        prefs.set(OptimizerWindowWidth, optimizerDialog.getSize().width);
                        prefs.set(OptimizerWindowHeight, optimizerDialog.getSize().height);
                        prefs.set(OptimizerWindowX, optimizerDialog.getX());
                        prefs.set(OptimizerWindowY, optimizerDialog.getY());
                    }
                    mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });

        mainViewDialog.forwardTestAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    Strategy strategy = createSelectedRowStrategy();
                    dispatcher.setMode(Mode.ForwardTest);
                    dispatcher.getTrader().getAssistant().addStrategy(strategy);
                } catch (Throwable t) {
                    MessageDialog.showError(t);
                } finally {
                    mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });

        mainViewDialog.tradeAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    Strategy strategy = createSelectedRowStrategy();
                    dispatcher.setMode(Mode.Trade);
                    dispatcher.getTrader().getAssistant().addStrategy(strategy);
                } catch (Throwable t) {
                    MessageDialog.showError(t);
                } finally {
                    mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });

        mainViewDialog.chartAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    int selectedRow = strategyTable.getSelectedRow();
                    if (selectedRow < 0) {
                        throw new JArbitragerException("No strategy is selected.");
                    }

                    Strategy strategy = strategyTableModel.getStrategyForRow(selectedRow);
                    if (strategy == null) {
                        String msg = "Please run this strategy first.";
                        throw new JArbitragerException(msg);
                    }

                    if (!strategy.getPerformanceChartData().isEmpty()) {
                        PerformanceChart pc = new PerformanceChart(mainViewDialog, strategy);
                        JFrame chartFrame = pc.getChart();
                        chartFrame.setVisible(true);
                    } else {
                        String msg = "There is no data to chart. Please run a back test first.";
                        MessageDialog.showMessage(msg);
                    }
                } catch (Throwable t) {
                    MessageDialog.showError(t);
                } finally {
                    mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });

        mainViewDialog.preferencesAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    new PreferencesDialog(mainViewDialog);
                } catch (Throwable t) {
                    MessageDialog.showError(t);
                } finally {
                    mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });

        mainViewDialog.discussionAction(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                openURL("http://groups.google.com/group/jarbitrager/topics");
            }
        });

        mainViewDialog.releaseNotesAction(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                openURL("http://code.google.com/p/jarbitrager/wiki/ReleaseNotes");
            }
        });

        mainViewDialog.projectHomeAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openURL("http://code.google.com/p/jarbitrager/");
            }
        });

        mainViewDialog.exitAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exit();
            }
        });

        mainViewDialog.exitAction(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });

        mainViewDialog.aboutAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    new AboutDialog(mainViewDialog);
                } catch (Throwable t) {
                    MessageDialog.showError(t);
                }
            }
        });
    }
}
