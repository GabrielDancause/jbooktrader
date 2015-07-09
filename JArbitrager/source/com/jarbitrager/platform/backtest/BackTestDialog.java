package com.jarbitrager.platform.backtest;

import com.jarbitrager.platform.model.*;
import static com.jarbitrager.platform.preferences.JArbitragerPreferences.*;
import com.jarbitrager.platform.preferences.*;
import com.jarbitrager.platform.startup.*;
import com.jarbitrager.platform.strategy.*;
import com.jarbitrager.platform.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

/**
 * Dialog to specify options for back testing using a historical data file.
 */
public class BackTestDialog extends JDialog {
    private static final Dimension MIN_SIZE = new Dimension(550, 130);

    private final PreferencesHolder prefs;
    private final Strategy strategy;
    private JButton cancelButton, backTestButton, selectFileButton;
    private JTextField fileNameText;
    private JProgressBar progressBar;
    private BackTestStrategyRunner btsr;

    public BackTestDialog(JFrame parent, Strategy strategy) {
        super(parent);
        this.strategy = strategy;
        prefs = PreferencesHolder.getInstance();
        init();
        pack();
        assignListeners();

        setLocationRelativeTo(parent);
        setVisible(true);
    }

    public void dispose() {
        btsr = null;
        super.dispose();
    }

    public void setProgress(long count, long iterations) {
        int percent = (int) (100 * (count / (double) iterations));
        progressBar.setValue(percent);
        progressBar.setString("Running back test: " + percent + "%");
    }

    public void enableProgress() {
        progressBar.setValue(0);
        progressBar.setString("Starting back test...");
        progressBar.setVisible(true);
        backTestButton.setEnabled(false);
        cancelButton.setEnabled(true);
        getRootPane().setDefaultButton(cancelButton);
    }

    public void showProgress(String progressText) {
        progressBar.setValue(0);
        progressBar.setString(progressText);
    }

    private void assignListeners() {

        backTestButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    prefs.set(BackTesterFileName, fileNameText.getText());
                    String historicalFileName = fileNameText.getText();
                    File file = new File(historicalFileName);
                    if (!file.exists()) {
                        fileNameText.requestFocus();
                        String msg = "Historical file " + "\"" + historicalFileName + "\"" + " does not exist.";
                        throw new JArbitragerException(msg);
                    }
                    btsr = new BackTestStrategyRunner(BackTestDialog.this, strategy);
                    new Thread(btsr).start();
                } catch (Exception ex) {
                    MessageDialog.showError(ex);
                }
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (btsr != null) {
                    btsr.cancel();
                }
                dispose();
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (btsr != null) {
                    btsr.cancel();
                }
            }
        });

        selectFileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser(JArbitrager.getAppPath());
                fileChooser.setDialogTitle("Select Historical Data File");

                String filename = getFileName();
                if (filename.length() != 0) {
                    fileChooser.setSelectedFile(new File(filename));
                }

                if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    fileNameText.setText(file.getAbsolutePath());
                }
            }
        });
    }

    private void init() {
        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("Back Test - " + strategy.getName());
        setLayout(new BorderLayout());

        JPanel northPanel = new JPanel(new SpringLayout());
        JLabel fileNameLabel = new JLabel("Historical data file:", JLabel.TRAILING);
        fileNameText = new JTextField();
        fileNameText.setText(prefs.get(BackTesterFileName));
        selectFileButton = new JButton(".");
        selectFileButton.setPreferredSize(new Dimension(27, 24));
        selectFileButton.setBackground(Color.BLUE);
        fileNameLabel.setLabelFor(fileNameText);
        northPanel.add(fileNameLabel);
        northPanel.add(fileNameText);
        northPanel.add(selectFileButton);
        SpringUtilities.makeTopOneLineGrid(northPanel);

        JPanel centerPanel = new JPanel(new SpringLayout());
        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        progressBar.setStringPainted(true);
        centerPanel.add(progressBar);
        SpringUtilities.makeOneLineGrid(centerPanel);

        JPanel southPanel = new JPanel();
        backTestButton = new JButton("Back Test");
        backTestButton.setMnemonic('B');
        cancelButton = new JButton("Cancel");
        cancelButton.setMnemonic('C');
        southPanel.add(backTestButton);
        southPanel.add(cancelButton);

        add(northPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(backTestButton);
        setPreferredSize(MIN_SIZE);
        setMinimumSize(getPreferredSize());
    }

    public String getFileName() {
        return fileNameText.getText();
    }

}
