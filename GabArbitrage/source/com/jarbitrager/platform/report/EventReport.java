package com.jarbitrager.platform.report;

import com.jarbitrager.platform.model.*;
import com.jarbitrager.platform.startup.*;

import java.io.*;
import java.text.*;
import java.util.*;


public class EventReport extends Report {
    private boolean isEnabled;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS z");

    public EventReport() throws JArbitragerException {
        super("EventReport");

        isEnabled = true;

        StringBuilder sb = new StringBuilder();
        sb.append(ROW_START);
        sb.append("<TH WIDTH=\"80\">").append("Date").append(HEADER_END);
        sb.append("<TH WIDTH=\"130\">").append("Time").append(HEADER_END);
        sb.append("<TH WIDTH=\"130\">").append("Reporter").append(HEADER_END);
        sb.append(HEADER_START).append("Message").append(HEADER_END);
        sb.append(ROW_END);
        write(sb);

        StringBuilder startupMessage = new StringBuilder();
        startupMessage.append("New Report Started. ").append(JArbitrager.APP_NAME).append(" version ").append(JArbitrager.VERSION);
        report(JArbitrager.APP_NAME, startupMessage);
    }

    public void disable() {
        isEnabled = false;
    }

    public void enable() {
        isEnabled = true;
    }

    private void report(String reporter, StringBuilder message) {
        Date date = getDate();
        StringBuilder s = new StringBuilder();
        s.append(ROW_START);
        s.append(FIELD_START).append(dateFormat.format(date)).append(FIELD_END);
        s.append(FIELD_START).append(timeFormat.format(date)).append(FIELD_END);
        s.append(FIELD_START).append(reporter).append(FIELD_END);
        s.append(FIELD_START).append(message).append(FIELD_END);
        s.append(ROW_END);
        write(s);
    }

    public void report(String reporter, String message) {
        if (isEnabled) {
            report(reporter, new StringBuilder(message));
        }
    }

    public void report(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        pw.close();
        report(JArbitrager.APP_NAME, new StringBuilder(sw.toString()));
    }

    private Date getDate() {
        return new Date();
    }
}
