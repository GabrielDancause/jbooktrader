package com.jarbitrager.platform.dialog;

import com.jarbitrager.platform.model.*;
import static com.jarbitrager.platform.model.StrategyTableColumn.*;
import com.jarbitrager.platform.startup.*;
import com.jarbitrager.platform.strategy.*;
import com.jarbitrager.platform.util.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;

/**
 * Main application window. All the system logic is intentionally left out if
 * this class, which acts as a simple "view" of the underlying model.
 */
public class MainFrameDialog extends JFrame implements ModelListener {
    private final Dispatcher dispatcher;
    private final Toolkit toolkit;
    private JMenuItem exitMenuItem, aboutMenuItem, discussionMenuItem, releaseNotesMenuItem, projectHomeMenuItem, preferencesMenuItem;
    private JMenuItem infoMenuItem, tradeMenuItem, backTestMenuItem, forwardTestMenuItem, optimizeMenuItem, chartMenuItem;
    private StrategyTableModel strategyTableModel;
    private JTable strategyTable;
    private JPopupMenu popupMenu;

    public MainFrameDialog() throws JArbitragerException {
        dispatcher = Dispatcher.getInstance();
        toolkit = Toolkit.getDefaultToolkit();
        init();
        populateStrategies();
        setVisible(true);
    }

    public void modelChanged(ModelListener.Event event, Object value) {
        switch (event) {
            case ModeChanged:
                String subTitle = dispatcher.getMode().getName();
                setTitle(JArbitrager.APP_NAME + " - [" + subTitle + "]");
                break;
            case Error:
                String msg = (String) value;
                MessageDialog.showError(msg);
                break;
            case StrategyUpdate:
                Strategy strategy = (Strategy) value;
                strategyTableModel.update(strategy);
                break;
            case StrategiesStart:
                Mode mode = dispatcher.getMode();
                if (mode == Mode.Trade) {
                    forwardTestMenuItem.setEnabled(false);
                }
                if (mode == Mode.ForwardTest) {
                    tradeMenuItem.setEnabled(false);
                }

                backTestMenuItem.setEnabled(false);
                optimizeMenuItem.setEnabled(false);
                chartMenuItem.setEnabled(true);
                break;
            case StrategiesEnd:
                forwardTestMenuItem.setEnabled(true);
                tradeMenuItem.setEnabled(true);
                backTestMenuItem.setEnabled(true);
                optimizeMenuItem.setEnabled(true);
                break;
        }
    }

    public void discussionAction(ActionListener action) {
        discussionMenuItem.addActionListener(action);
    }

    public void releaseNotesAction(ActionListener action) {
        releaseNotesMenuItem.addActionListener(action);
    }

    public void projectHomeAction(ActionListener action) {
        projectHomeMenuItem.addActionListener(action);
    }

    public void strategyTableAction(MouseAdapter action) {
        strategyTable.addMouseListener(action);
    }

    public void informationAction(ActionListener action) {
        infoMenuItem.addActionListener(action);
    }

    public void backTestAction(ActionListener action) {
        backTestMenuItem.addActionListener(action);
    }

    public void optimizeAction(ActionListener action) {
        optimizeMenuItem.addActionListener(action);
    }

    public void forwardTestAction(ActionListener action) {
        forwardTestMenuItem.addActionListener(action);
    }

    public void tradeAction(ActionListener action) {
        tradeMenuItem.addActionListener(action);
    }

    public void chartAction(ActionListener action) {
        chartMenuItem.addActionListener(action);
    }

    public void preferencesAction(ActionListener action) {
        preferencesMenuItem.addActionListener(action);
    }

    public void exitAction(ActionListener action) {
        exitMenuItem.addActionListener(action);
    }

    public void exitAction(WindowAdapter action) {
        addWindowListener(action);
    }

    public void aboutAction(ActionListener action) {
        aboutMenuItem.addActionListener(action);
    }

    private URL getImageURL(String imageFileName) throws JArbitragerException {
        URL imgURL = ClassLoader.getSystemResource(imageFileName);
        if (imgURL == null) {
            String msg = "Could not locate " + imageFileName + ". Make sure the /resources directory is in the classpath.";
            throw new JArbitragerException(msg);
        }
        return imgURL;
    }

    private ImageIcon getImageIcon(String imageFileName) throws JArbitragerException {
        return new ImageIcon(toolkit.getImage(getImageURL(imageFileName)));
    }

    private void populateStrategies() {
        for (Strategy strategy : ClassFinder.getStrategies()) {
            strategyTableModel.addStrategy(strategy);
        }
    }

    public StrategyTableModel getStrategyTableModel() {
        return strategyTableModel;
    }

    public JTable getStrategyTable() {
        return strategyTable;
    }

    public void showPopup(MouseEvent mouseEvent) {
        popupMenu.show(strategyTable, mouseEvent.getX(), mouseEvent.getY());
    }

    private void init() throws JArbitragerException {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        // session menu
        JMenu sessionMenu = new JMenu("Session");
        sessionMenu.setMnemonic('S');
        exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.setMnemonic('X');
        sessionMenu.add(exitMenuItem);

        // configure menu
        JMenu configureMenu = new JMenu("Configure");
        configureMenu.setMnemonic('C');
        preferencesMenuItem = new JMenuItem("Preferences");
        preferencesMenuItem.setMnemonic('P');
        configureMenu.add(preferencesMenuItem);

        // help menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('H');
        releaseNotesMenuItem = new JMenuItem("Release Notes");
        releaseNotesMenuItem.setMnemonic('R');
        discussionMenuItem = new JMenuItem("Discussion Group");
        discussionMenuItem.setMnemonic('D');
        projectHomeMenuItem = new JMenuItem("Project Home");
        projectHomeMenuItem.setMnemonic('P');
        aboutMenuItem = new JMenuItem("About...");
        aboutMenuItem.setMnemonic('A');
        helpMenu.addSeparator();
        helpMenu.add(releaseNotesMenuItem);
        helpMenu.add(discussionMenuItem);
        helpMenu.add(projectHomeMenuItem);
        helpMenu.addSeparator();
        helpMenu.add(aboutMenuItem);

        // menu bar
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(sessionMenu);
        menuBar.add(configureMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);

        // popup menu
        popupMenu = new JPopupMenu();

        infoMenuItem = new JMenuItem("Information", getImageIcon("information.png"));
        backTestMenuItem = new JMenuItem("Back Test", getImageIcon("backTest.png"));
        optimizeMenuItem = new JMenuItem("Optimize", getImageIcon("optimize.png"));
        forwardTestMenuItem = new JMenuItem("Forward Test", getImageIcon("forwardTest.png"));
        tradeMenuItem = new JMenuItem("Trade");
        chartMenuItem = new JMenuItem("Chart", getImageIcon("chart.png"));

        popupMenu.add(infoMenuItem);
        popupMenu.addSeparator();
        popupMenu.add(optimizeMenuItem);
        popupMenu.add(backTestMenuItem);
        popupMenu.add(forwardTestMenuItem);
        popupMenu.add(chartMenuItem);
        popupMenu.addSeparator();
        popupMenu.add(tradeMenuItem);

        JScrollPane strategyTableScrollPane = new JScrollPane();
        strategyTableScrollPane.setAutoscrolls(true);
        strategyTableModel = new StrategyTableModel();
        strategyTable = new JTable(strategyTableModel);
        strategyTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // set custom column renderers
        TableColumnModel columnModel = strategyTable.getColumnModel();

        NumberRenderer nr0 = new NumberRenderer(0);
        columnModel.getColumn(NetProfit.ordinal()).setCellRenderer(nr0);
        columnModel.getColumn(MaxDD.ordinal()).setCellRenderer(nr0);
        DefaultTableCellRenderer tableCellRenderer = new DefaultTableCellRenderer();
        tableCellRenderer.setHorizontalAlignment(JLabel.RIGHT);
        columnModel.getColumn(Indicators.ordinal()).setCellRenderer(tableCellRenderer);

        // Make some columns wider than the rest, so that the info fits in.
        columnModel.getColumn(Strategy.ordinal()).setPreferredWidth(100);

        for (int column = 1; column <= 4; column++) {
            columnModel.getColumn(column).setCellRenderer(new ColorColumnRenderer(Color.getHSBColor(0.85f, 0.9f, 0.6f)));
        }

        for (int column = 5; column <= 8; column++) {
            columnModel.getColumn(column).setCellRenderer(new ColorColumnRenderer(Color.getHSBColor(0.7f, 0.9f, 0.6f)));
        }

        strategyTableScrollPane.getViewport().add(strategyTable);

        Image appIcon = Toolkit.getDefaultToolkit().getImage(getImageURL("JArbitrager.png"));
        setIconImage(appIcon);

        add(strategyTableScrollPane, BorderLayout.CENTER);

        JToolBar statusBar = new JToolBar();
        statusBar.setLayout(new BorderLayout());
        statusBar.setFloatable(false);

        statusBar.add(new JLabel(" "), BorderLayout.EAST);
        add(statusBar, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(500, 309));
        setTitle(JArbitrager.APP_NAME);
        pack();
        setLocationRelativeTo(null);
    }
}
