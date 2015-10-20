package com.jarbitrager.platform.web;

import com.jarbitrager.platform.model.*;
import static com.jarbitrager.platform.preferences.JArbitragerPreferences.*;
import com.jarbitrager.platform.preferences.*;
import com.jarbitrager.platform.report.*;
import com.jarbitrager.platform.startup.*;
import com.jarbitrager.platform.util.*;
import com.sun.net.httpserver.*;

import java.net.*;
import java.util.concurrent.*;

public class MonitoringServer {
    private static HttpServer server;

    public static void start() {
        if (server == null) {
            PreferencesHolder prefs = PreferencesHolder.getInstance();
            if (prefs.get(WebAccess).equalsIgnoreCase("enabled")) {
                EventReport eventReport = Dispatcher.getInstance().getEventReport();
                try {
                    int port = Integer.parseInt(prefs.get(WebAccessPort));
                    server = HttpServer.create(new InetSocketAddress(port), 0);
                    HttpContext context = server.createContext("/", new WebHandler());
                    context.setAuthenticator(new WebAuthenticator());
                    server.setExecutor(Executors.newSingleThreadExecutor());
                    server.start();
                    eventReport.report(JArbitrager.APP_NAME, "Monitoring server started");
                } catch (Exception e) {
                    eventReport.report(e);
                    MessageDialog.showError("Could not start monitoring server: " + e);
                }
            }
        }
    }
}
