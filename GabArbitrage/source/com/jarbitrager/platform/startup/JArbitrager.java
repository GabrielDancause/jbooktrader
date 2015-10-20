package com.jarbitrager.platform.startup;

import com.birosoft.liquid.*;
import com.jarbitrager.platform.model.*;
import com.jarbitrager.platform.util.*;

import javax.swing.*;
import javax.swing.plaf.*;
import java.io.*;
import java.nio.channels.*;

/**
 * Application starter.
 */
public class JArbitrager {
    public static final String APP_NAME = "JArbitrager";
    public static final String VERSION = "1.02";
    public static final String RELEASE_DATE = "July 8, 2010";
    private static String appPath;

    /**
     * Instantiates the necessary parts of the application: the application
     * model, views, and controller.
     */
    private JArbitrager() throws JArbitragerException, UnsupportedLookAndFeelException {
        LiquidLookAndFeel.setLiquidDecorations(true, "mac");
        UIManager.setLookAndFeel(new LiquidLookAndFeel());

        // Set the color scheme explicitly
        ColorUIResource color = new ColorUIResource(102, 102, 153);
        UIManager.put("Label.foreground", color);
        UIManager.put("TitledBorder.titleColor", color);

        Dispatcher.getInstance().setReporter();
        new MainFrameController();
    }

    /**
     * Starts JArbitrager application.
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            File file = new File(System.getProperty("user.home"), APP_NAME + ".tmp");
            FileChannel channel = new RandomAccessFile(file, "rw").getChannel();

            if (channel.tryLock() == null) {
                MessageDialog.showMessage(APP_NAME + " is already running.");
                return;
            }

            if (args.length != 1) {
                String msg = "Exactly one argument must be passed. Usage: JArbitrager <JArbitragerDirectory>";
                throw new JArbitragerException(msg);
            }
            JArbitrager.appPath = args[0];
            new JArbitrager();
        } catch (Throwable t) {
            MessageDialog.showError(t);
        }
    }

    public static String getAppPath() {
        return JArbitrager.appPath;
    }

}
