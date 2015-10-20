package com.jarbitrager.platform.dialog;

import com.ib.client.*;
import com.jarbitrager.platform.model.*;
import com.jarbitrager.platform.startup.*;
import com.jarbitrager.platform.trader.*;
import com.jarbitrager.platform.util.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * Dialog to show the application info, system info, and IB API info.
 */
public class AboutDialog extends JDialog {

    /* inner class to define the "about" model */
    private class AboutTableModel extends TableDataModel {
        private AboutTableModel() {
            String[] aboutSchema = {"Property", "Value"};
            setSchema(aboutSchema);
        }
    }

    public AboutDialog(JFrame parent) {
        super(parent);
        init();
        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    private void init() {
        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("About " + JArbitrager.APP_NAME);

        JPanel contentPanel = new JPanel(new BorderLayout());
        getContentPane().add(contentPanel, BorderLayout.CENTER);

        JTabbedPane tabbedPane = new JTabbedPane();
        contentPanel.add(tabbedPane, BorderLayout.CENTER);

        JPanel aboutPanel = new JPanel(new SpringLayout());
        JPanel apiPanel = new JPanel(new SpringLayout());

        tabbedPane.addTab("About", aboutPanel);
        tabbedPane.addTab("API Info", apiPanel);

        JLabel productLabel = new JLabel("Product:", JLabel.TRAILING);
        JLabel productValueLabel = new JLabel(JArbitrager.APP_NAME);
        productValueLabel.setForeground(Color.BLACK);
        productLabel.setLabelFor(productValueLabel);
        aboutPanel.add(productLabel);
        aboutPanel.add(productValueLabel);

        JLabel versionLabel = new JLabel("Version:", JLabel.TRAILING);
        JLabel versionValueLabel = new JLabel(JArbitrager.VERSION);
        versionValueLabel.setForeground(Color.BLACK);
        versionLabel.setLabelFor(versionValueLabel);
        aboutPanel.add(versionLabel);
        aboutPanel.add(versionValueLabel);

        JLabel releaseDateLabel = new JLabel("Released:", JLabel.TRAILING);
        JLabel releaseDateValueLabel = new JLabel(JArbitrager.RELEASE_DATE);
        releaseDateValueLabel.setForeground(Color.BLACK);
        releaseDateLabel.setLabelFor(releaseDateValueLabel);
        aboutPanel.add(releaseDateLabel);
        aboutPanel.add(releaseDateValueLabel);

        JLabel authorLabel = new JLabel("Author:", JLabel.TRAILING);
        JLabel authorValueLabel = new JLabel("Eugene Kononov");
        authorValueLabel.setForeground(Color.BLACK);
        authorLabel.setLabelFor(authorValueLabel);
        aboutPanel.add(authorLabel);
        aboutPanel.add(authorValueLabel);

        JLabel emailLabel = new JLabel("Email:", JLabel.TRAILING);
        JLabel emailValueLabel = new JLabel("eugene.kononov@gmail.com");
        emailValueLabel.setForeground(Color.BLACK);
        emailLabel.setLabelFor(productValueLabel);
        aboutPanel.add(emailLabel);
        aboutPanel.add(emailValueLabel);

        JLabel licenseLabel = new JLabel("License:", JLabel.TRAILING);
        JLabel licenseValueLabel = new JLabel("BSD (Free, open source)");
        licenseValueLabel.setForeground(Color.BLACK);
        licenseLabel.setLabelFor(licenseValueLabel);
        aboutPanel.add(licenseLabel);
        aboutPanel.add(licenseValueLabel);

        SpringUtilities.makeCompactGrid(aboutPanel, 6, 2, 12, 12, 5, 5);

        JLabel serverVersionLabel = new JLabel("Server Version:", JLabel.TRAILING);
        String serverVersion = "Disconnected from server";
        Trader trader = Dispatcher.getInstance().getTrader();
        if (trader != null) {
            int version = trader.getAssistant().getServerVersion();
            if (version != 0) {
                serverVersion = String.valueOf(version);
            }
        }
        JLabel serverVersionValueLabel = new JLabel(serverVersion);
        serverVersionValueLabel.setForeground(Color.BLACK);
        serverVersionLabel.setLabelFor(serverVersionValueLabel);
        apiPanel.add(serverVersionLabel);
        apiPanel.add(serverVersionValueLabel);

        JLabel clientVersionLabel = new JLabel("Client Version:", JLabel.TRAILING);
        JLabel clientVersionValueLabel = new JLabel("" + EClientSocket.CLIENT_VERSION);
        clientVersionValueLabel.setForeground(Color.BLACK);
        clientVersionLabel.setLabelFor(clientVersionValueLabel);
        apiPanel.add(clientVersionLabel);
        apiPanel.add(clientVersionValueLabel);

        SpringUtilities.makeCompactGrid(apiPanel, 2, 2, 12, 12, 5, 5);

        JPanel systemInfoPanel = new JPanel(new BorderLayout(5, 5));
        tabbedPane.addTab("System Info", systemInfoPanel);

        JScrollPane systemInfoScrollPane = new JScrollPane();
        systemInfoPanel.add(systemInfoScrollPane, BorderLayout.CENTER);

        TableDataModel aboutModel = new AboutTableModel();
        systemInfoScrollPane.getViewport().add(new JTable(aboutModel));

        getContentPane().setPreferredSize(new Dimension(450, 400));

        Properties properties = System.getProperties();
        Enumeration<?> propNames = properties.propertyNames();

        while (propNames.hasMoreElements()) {
            String key = (String) propNames.nextElement();
            String value = properties.getProperty(key);
            String[] row = {key, value};
            aboutModel.addRow(row);
        }
    }
}
