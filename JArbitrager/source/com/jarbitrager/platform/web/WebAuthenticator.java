package com.jarbitrager.platform.web;

import static com.jarbitrager.platform.preferences.JArbitragerPreferences.*;
import com.jarbitrager.platform.preferences.*;
import com.jarbitrager.platform.startup.*;
import com.sun.net.httpserver.*;

public class WebAuthenticator extends BasicAuthenticator {
    private final String authPair;

    public WebAuthenticator() {
        super(JArbitrager.APP_NAME);
        PreferencesHolder prefs = PreferencesHolder.getInstance();
        authPair = prefs.get(WebAccessUser) + "/" + prefs.get(WebAccessPassword);
    }

    public boolean checkCredentials(String userName, String password) {
        return authPair.equals(userName + "/" + password);
    }
}