package com.jarbitrager.platform.util;

import com.jarbitrager.platform.model.*;
import com.jarbitrager.platform.startup.*;

import javax.swing.*;

/**
 * Utility class to display message and error dialogs.
 */
public class MessageDialog {

    public static void showMessage(String msg) {
        JOptionPane.showMessageDialog(null, msg, JArbitrager.APP_NAME, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(String msg) {
        JOptionPane.showMessageDialog(null, msg, JArbitrager.APP_NAME, JOptionPane.ERROR_MESSAGE);
    }

    public static void showError(Throwable t) {
        Dispatcher.getInstance().getEventReport().report(t);
        showError(t.getMessage());
    }
}
