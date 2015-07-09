package com.jarbitrager.platform.web;

import com.jarbitrager.platform.instrument.*;
import com.jarbitrager.platform.model.*;
import com.jarbitrager.platform.performance.*;
import com.jarbitrager.platform.startup.*;
import com.jarbitrager.platform.strategy.*;
import com.jarbitrager.platform.util.*;
import com.sun.net.httpserver.*;

import java.io.*;
import java.net.*;
import java.text.*;

public class WebHandler implements HttpHandler {
    protected static final String FIELD_START = "<td align=\"right\">";
    protected static final String FIELD_END = "</td>";
    protected static final String HEADER_START = "<th>";
    protected static final String HEADER_END = "</th>";
    protected static final String ROW_START = "<tr>";
    protected static final String ROW_END = "</tr>";
    private final Dispatcher dispatcher;
    private final StringBuilder staticContent;
    private final StringBuilder response;
    private final DecimalFormat df = NumberFormatterFactory.getNumberFormatter(0);

    public WebHandler() {
        dispatcher = Dispatcher.getInstance();
        response = new StringBuilder();
        staticContent = new StringBuilder();
        staticContent.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">");
        staticContent.append("<html>");
        staticContent.append("<head>");
        staticContent.append("<title>JArbitrager Web Console</title>");
        staticContent.append("<style type=\"text/css\">h3 { text-align: center; }</style>");
        staticContent.append("<meta http-equiv=\"refresh\" content=\"60\">");
        staticContent.append("</head>");
        staticContent.append("<body>");
        staticContent.append("<h3>");
        staticContent.append(JArbitrager.APP_NAME).append(", version ").append(JArbitrager.VERSION);
        staticContent.append(", ").append(dispatcher.getMode()).append(" mode");
        staticContent.append("</h3>");
        staticContent.append("<table bgcolor=\"#FFFFEE\" cellspacing=\"0\" border=\"1\" width=\"100%\">");
        staticContent.append("<tr bgcolor=\"#FFCC33\"><th>Strategy<th>Symbol<th>Bid<th>Ask<th>Position<th>Symbol<th>Bid<th>Ask<th>Position<th>Trades<th>Max DD<th>Net Profit</tr>");
    }

    public void addRow(Object content) {
        response.append(FIELD_START).append(content).append("</td>");
    }

    public void handle(HttpExchange httpExchange) throws IOException {
        response.setLength(0);
        response.append(staticContent);

        for (Strategy strategy : dispatcher.getTrader().getAssistant().getAllStrategies()) {
            PerformanceManager performanceManager = strategy.getPerformanceManager();
            response.append("<tr>");
            response.append("<td>").append(strategy.getName()).append("</td>");

            Instrument instrument1 = strategy.getInstrument1();
            Instrument instrument2 = strategy.getInstrument2();

            addRow(instrument1.getContract().m_symbol);
            addRow(instrument1.getBid());
            addRow(instrument1.getAsk());
            addRow(instrument1.getCurrentPosition());

            addRow(instrument2.getContract().m_symbol);
            addRow(instrument2.getBid());
            addRow(instrument2.getAsk());
            addRow(instrument2.getCurrentPosition());

            addRow(performanceManager.getTrades());
            addRow(df.format(performanceManager.getMaxDrawdown()));
            addRow(df.format(performanceManager.getNetProfit()));
            response.append("</tr>");
        }

        response.append("</table>");
        response.append("</body>");
        response.append("</html>");

        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.toString().getBytes());
        os.close();
    }
}
