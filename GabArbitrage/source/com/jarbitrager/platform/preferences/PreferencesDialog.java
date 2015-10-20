package com.jarbitrager.platform.preferences;

import com.jarbitrager.platform.model.*;
import static com.jarbitrager.platform.preferences.JArbitragerPreferences.*;
import com.jarbitrager.platform.startup.*;
import com.jarbitrager.platform.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class PreferencesDialog extends JDialog {
    private static final Dimension FIELD_DIMENSION = new Dimension(Integer.MAX_VALUE, 22);
    private final PreferencesHolder prefs;
    private JTextField hostText, portText, webAccessUser;
    private JSpinner clientIDSpin, webAccessPortSpin;
    private JPasswordField webAccessPasswordField;
    private JComboBox webAccessCombo;

    public PreferencesDialog(JFrame parent) throws JArbitragerException {
        super(parent);
        prefs = PreferencesHolder.getInstance();
        init();
        pack();
        setLocationRelativeTo(parent);
        setModal(true);
        setVisible(true);
    }

    private void add(JPanel panel, JArbitragerPreferences pref, JTextField textField) {
        textField.setText(prefs.get(pref));
        genericAdd(panel, pref, textField);
    }

    private void add(JPanel panel, JArbitragerPreferences pref, JSpinner spinner) {
        spinner.setValue(prefs.getInt(pref));
        genericAdd(panel, pref, spinner);
    }

    private void add(JPanel panel, JArbitragerPreferences pref, JComboBox comboBox) {
        comboBox.setSelectedItem(prefs.get(pref));
        genericAdd(panel, pref, comboBox);
    }

    private void genericAdd(JPanel panel, JArbitragerPreferences pref, Component comp) {
        JLabel fieldNameLabel = new JLabel(pref.getName() + ":");
        fieldNameLabel.setLabelFor(comp);
        comp.setPreferredSize(FIELD_DIMENSION);
        comp.setMaximumSize(FIELD_DIMENSION);
        panel.add(fieldNameLabel);
        panel.add(comp);
    }

    private void init() throws JArbitragerException {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("Preferences");

        JPanel contentPanel = new JPanel(new BorderLayout());

        JPanel buttonsPanel = new JPanel();
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

        JTabbedPane tabbedPane1 = new JTabbedPane();
        contentPanel.add(tabbedPane1, BorderLayout.CENTER);

        JPanel connectionTab = new JPanel(new SpringLayout());
        tabbedPane1.addTab("TWS Connection", connectionTab);
        hostText = new JTextField();
        portText = new JTextField();
        clientIDSpin = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
        add(connectionTab, Host, hostText);
        add(connectionTab, Port, portText);
        add(connectionTab, ClientID, clientIDSpin);
        SpringUtilities.makeCompactGrid(connectionTab, 3, 2, 12, 12, 8, 5);
        setWidth(connectionTab, clientIDSpin, 45);

        JPanel webAcessTab = new JPanel(new SpringLayout());
        tabbedPane1.addTab("Web Access", webAcessTab);
        webAccessCombo = new JComboBox(new String[] {"disabled", "enabled"});
        webAccessPortSpin = new JSpinner(new SpinnerNumberModel(1, 1, 99999, 1));
        webAccessUser = new JTextField();
        webAccessPasswordField = new JPasswordField();
        add(webAcessTab, WebAccess, webAccessCombo);
        add(webAcessTab, WebAccessPort, webAccessPortSpin);
        add(webAcessTab, WebAccessUser, webAccessUser);
        add(webAcessTab, WebAccessPassword, webAccessPasswordField);
        SpringUtilities.makeCompactGrid(webAcessTab, 4, 2, 12, 12, 8, 5);

        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    prefs.set(Host, hostText.getText());
                    prefs.set(Port, portText.getText());
                    prefs.set(ClientID, clientIDSpin.getValue().toString());

                    prefs.set(WebAccess, (String) webAccessCombo.getSelectedItem());
                    prefs.set(WebAccessPort, webAccessPortSpin.getValue().toString());
                    prefs.set(WebAccessUser, webAccessUser.getText());
                    prefs.set(WebAccessPassword, new String(webAccessPasswordField.getPassword()));

                    String msg = "Some of the preferences will not take effect until " + JArbitrager.APP_NAME + " is restarted.";
                    MessageDialog.showMessage(msg);

                    dispose();
                } catch (Exception ex) {
                    MessageDialog.showError(ex);
                }
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        setPreferredSize(new Dimension(500, 380));
    }

    private void setWidth(JPanel p, Component c, int width) throws JArbitragerException {
        try {
            SpringLayout layout = (SpringLayout) p.getLayout();
            SpringLayout.Constraints spinLayoutConstraint = layout.getConstraints(c);
            spinLayoutConstraint.setWidth(Spring.constant(width));
        } catch (ClassCastException exc) {
            throw new JArbitragerException("The first argument to makeGrid must use SpringLayout.");
        }
    }
}
